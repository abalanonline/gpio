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

  private final Gpio b1;
  private final Gpio b2;
  private final Gpio b;
  private final BusyRunner runner;

  public RotaryEncoder(Gpio b1, Gpio b2, Gpio b, BusyRunner runner) {
    this.b1 = b1;
    this.b2 = b2;
    this.b = b;
    this.runner = runner;
  }

  @Override
  public void run() {

  }

  @Override
  public RotaryEncoder open() {
    b1.open();
    b2.open();
    b.open();
    return this;
  }

  @Override
  public void close() {
    b1.close();
    b2.close();
    b.close();
  }

}
