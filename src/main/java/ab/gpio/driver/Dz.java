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

package ab.gpio.driver;

import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;
import com.diozero.internal.provider.builtin.gpio.GpioChip;
import com.diozero.internal.provider.builtin.gpio.GpioLine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * apt install gpiod
 *
 * 775 fps
 *
 * @deprecated Use {@link ab.gpio.Pin} instead.
 */
@Deprecated
public class Dz implements Gpio {
  static Map<Integer, GpioChip> chips;
  static {
    try {
      chips = GpioChip.openAllChips();
    } catch (IOException e) {
      throw new UncheckedIOException("Error initialising GPIO chips", e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> chips.values().forEach(GpioChip::close)));
  }

  private final int chip;
  private final int offset;
  private final boolean readOnly;
  private GpioLine line;

  public Dz(int chip, int offset) {
    this(chip, offset, false);
  }

  public Dz(int chip, int offset, boolean readOnly) {
    this.chip = chip;
    this.offset = offset;
    this.readOnly = readOnly;
  }

  @Override
  public Dz open() {
    if (line != null) throw new IllegalStateException("not closed");
    GpioChip gpioChip = chips.get(chip);
    line = readOnly ? gpioChip.provisionGpioInputDevice(offset, GpioPullUpDown.NONE, GpioEventTrigger.NONE)
        : gpioChip.provisionGpioOutputDevice(offset, 0);
    return this;
  }

  @Override
  public void close() {
    GpioLine line = this.line;
    if (line == null) return;
    line.close();
    this.line = null;
  }

  @Override
  public void set(boolean v) {
    if (readOnly) throw new IllegalStateException();
    line.setValue(v ? 1 : 0);
  }

  @Override
  public boolean get() {
    return line.getValue() != 0;
  }

}
