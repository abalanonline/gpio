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

import com.diozero.internal.provider.builtin.gpio.GpioChip;
import com.diozero.internal.provider.builtin.gpio.GpioLine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * apt install gpiod
 *
 * 775 fps
 */
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
  private GpioLine line;

  public Dz(int chip, int offset) {
    this.chip = chip;
    this.offset = offset;
  }

  @Override
  public void open() {
    if (line != null) throw new IllegalStateException("not closed");
    line = chips.get(chip).provisionGpioOutputDevice(offset, 0);
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
    line.setValue(v ? 1 : 0);
  }

  @Override
  public boolean get() {
    return line.getValue() != 0;
  }

}
