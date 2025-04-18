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

public class Pwm implements BusyRunnable {
  private int i = 0;
  private int pw = 0;
  private int t = 1;
  private boolean open;
  private final Pin pin;
  private final BusyRunner runner;

  public Pwm(Pin pin, BusyRunner runner) {
    this.pin = pin;
    this.runner = runner;
  }

  /**
   * 0% and 100% duty cycle applied immediately
   */
  public void setDutyCycle(int pw, int t) {
    if (open && pw <= 0) pin.set(false); else if (open && pw >= t) pin.set(true);
    this.pw = pw;
    this.t = t;
  }

  @Override
  public void run() {
    if (!open) return;
    if (i >= t) i = 0;
    pin.set(i++ < pw);
  }

  @Override
  public Pwm open() {
    if (open) throw new IllegalStateException();
    open = true;
    pin.open();
    runner.add(this);
    return this;
  }

  @Override
  public void close() {
    open = false;
    runner.remove(this);
    pin.close();
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("java -cp .jar ab.gpio.Pwm 0 10");
      System.exit(1);
    }
    Pin pin = new Pin(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

    try (BusyRunner busyRunner = new BusyRunner().open();
        Pwm pwm = new Pwm(pin, busyRunner).open()) {
      for (int i = 0; i < 10; i++) {
        pwm.setDutyCycle(i + 1, 10);
        Thread.sleep(100);
      }
      for (int i = 0; i < 10; i++) {
        pwm.setDutyCycle(9 - i, 10);
        Thread.sleep(100);
      }
    } catch (InterruptedException ignore) {}
  }

}
