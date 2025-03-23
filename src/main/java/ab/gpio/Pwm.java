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

public class Pwm implements BusyRunnable {
  private int i = 0;
  private int pw = 0;
  private int t = 1;
  private final Gpio pin;
  private final BusyRunner runner;

  public Pwm(Gpio pin, BusyRunner runner) {
    this.pin = pin;
    this.runner = runner;
  }

  public void setDutyCycle(int pw, int t) {
    this.pw = pw;
    this.t = t;
  }

  @Override
  public void run() {
    if (i >= t) i = 0;
    pin.set(i++ < pw);
  }

  @Override
  public Pwm open() {
    pin.open();
    runner.add(this);
    return this;
  }

  @Override
  public void close() {
    runner.remove(this);
    pin.close();
  }
}
