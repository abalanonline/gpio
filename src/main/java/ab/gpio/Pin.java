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

import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;
import com.diozero.internal.provider.builtin.gpio.GpioChip;
import com.diozero.internal.provider.builtin.gpio.GpioLine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * The reference implementation of GPIO pin. Can be overridden by hardware specific implementations.
 */
public class Pin implements AutoCloseable {
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

  public Pin(int chip, int offset) {
    this(chip, offset, false);
  }

  public Pin(int chip, int offset, boolean readOnly) {
    this.chip = chip;
    this.offset = offset;
    this.readOnly = readOnly;
  }

  public Pin open() {
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

  public void set(boolean v) {
    if (readOnly) throw new IllegalStateException();
    line.setValue(v ? 1 : 0);
  }

  public boolean get() {
    return line.getValue() != 0;
  }

}
