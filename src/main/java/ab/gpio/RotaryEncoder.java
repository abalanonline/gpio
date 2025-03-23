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

public class RotaryEncoder implements BusyRunnable {

  private final Gpio clk;
  private final Gpio dt;
  private final Gpio sw;
  private final BusyRunner runner;
  private boolean clkv;
  public int v;

  public RotaryEncoder(Gpio clk, Gpio dt, Gpio sw, BusyRunner runner) {
    this.clk = clk;
    this.dt = dt;
    this.sw = sw;
    this.runner = runner;
  }

  @Override
  public void run() {
    boolean clkv = !clk.get();
    if (clkv && !this.clkv) v += dt.get() ? 1 : -1;
    this.clkv = clkv;
  }

  @Override
  public RotaryEncoder open() {
    clk.open();
    dt.open();
    sw.open();
    runner.add(this);
    return this;
  }

  @Override
  public void close() {
    runner.remove(this);
    clk.close();
    dt.close();
    sw.close();
  }

}
