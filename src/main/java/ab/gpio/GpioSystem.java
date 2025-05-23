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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GpioSystem {
  public static List<String> devicetreeCompatible() {
    Path path = Paths.get("/proc/device-tree/compatible");
    try {
      return Arrays.asList(Files.readString(path).split("\0"));
    } catch (IOException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Gets the item corresponding to the device name. Used for hardware dependent objects - pins numbers, devices.
   * Starts search from the first element of the list. Returns default item if device name not found.
   * @param deviceNames list of device names in /proc/device-tree/compatible format
   * @param items list of corresponding items
   * @param defaultItem default item
   */
  public static <T> T getByDevice(Collection<String> deviceNames, Collection<T> items, T defaultItem) {
    Set<String> set = new LinkedHashSet<>(devicetreeCompatible());
    Iterator<T> itemsIterator = items.iterator();
    for (String deviceName : deviceNames) {
      T item = itemsIterator.next();
      if (set.contains(deviceName)) return item;
    }
    java.util.logging.Logger.getAnonymousLogger().info("Using default item for board: " + String.join(", ", set));
    return defaultItem;
  }

}
