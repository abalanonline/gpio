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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The reference implementation of GPIO pin. Can be overridden by hardware specific implementations.
 */
public class Pin implements AutoCloseable {
  private static final Map<Integer, GpioChip> chips;
  static {
    int[] gpiochipIds;
    try {
      gpiochipIds = Files.list(Paths.get("/dev")).filter(p -> p.getFileName().toString().matches("gpiochip\\d+"))
          .filter(p -> !Files.isSymbolicLink(p)) // can return false for symlink, witnessed this once
          .mapToInt(p -> Integer.parseInt(p.getFileName().toString().substring(8))).sorted().toArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Error initialising GPIO chips", e);
    }
    HashMap<Integer, GpioChip> map = new HashMap<>();
    for (int gpiochipId : gpiochipIds) {
      GpioChip gpioChip = GpioChip.openChip("/dev/gpiochip" + gpiochipId);
      if (gpioChip == null || gpiochipId != gpioChip.getChipId()) continue;
      map.put(gpiochipId, gpioChip);
    }
    chips = Collections.unmodifiableMap(map);
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
