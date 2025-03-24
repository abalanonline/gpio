/*
 * Copyright (C) 2025 Aleksei Balan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ab.gpio;

import ab.gpio.driver.BusyRunnable;
import ab.gpio.driver.BusyRunner;
import ab.gpio.driver.Gpio;
import ab.tui.Tui;

import java.awt.Dimension;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class RotaryEncoder implements Tui, BusyRunnable {

  private final Gpio clk;
  private final Gpio dt;
  private final Gpio sw;
  private final BusyRunner runner;
  private final BlockingQueue<Integer> queue;
  private Consumer<String> keyListener;
  private boolean clkv;
  private boolean swv;
  private boolean open;

  public RotaryEncoder(Gpio clk, Gpio dt, Gpio sw, BusyRunner runner) {
    this.clk = clk;
    this.dt = dt;
    this.sw = sw;
    this.runner = runner;
    this.queue = new LinkedBlockingQueue<>();
  }

  @Override
  public void run() {
    boolean swv = !sw.get();
    if (swv && !this.swv) queue.add(2);
    boolean clkv = !clk.get();
    if (clkv && !this.clkv) queue.add(dt.get() ? 1 : -1);
    this.clkv = clkv;
    if (!swv && this.swv) queue.add(0);
    this.swv = swv;
  }

  @Override
  public Dimension getSize() {
    return new Dimension();
  }

  @Override
  public void print(int x, int y, String s, int attr) {
    // no output, do nothing
  }

  @Override
  public void update() {
    // no output, do nothing
  }

  @Override
  public void setKeyListener(Consumer<String> keyListener) {
    this.keyListener = keyListener;
  }

  protected void keyRun() {
    int v;
    boolean swv = false;
    while (open) {
      try {
        v = queue.take();
      } catch (InterruptedException e) {
        break;
      }
      if (!open) break;
      Consumer<String> keyListener = this.keyListener;
      if (keyListener == null) continue;
      switch (v) {
        case -1: keyListener.accept(swv ? "-" : "Left"); break;
        case 1: keyListener.accept(swv ? "+" : "Right"); break;
        case 0: keyListener.accept("0"); swv = false; break;
        case 2: keyListener.accept("1"); swv = true; break;
      }
    }
  }

  @Override
  public RotaryEncoder open() {
    if (open) throw new IllegalStateException("not closed");
    open = true;
    clk.open();
    dt.open();
    sw.open();
    runner.add(this);
    queue.clear();
    new Thread(this::keyRun).start();
    return this;
  }

  @Override
  public void close() {
    open = false;
    queue.add(0); // unblock queue
    runner.remove(this);
    clk.close();
    dt.close();
    sw.close();
  }

}
