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

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GpioSystemTest {

  @Test
  void devicetreeCompatible() {
    List<String> compatible;
    try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
      filesMockedStatic.when(() -> Files.readString(any())).thenReturn("raspberrypi,4-model-b\0brcm,bcm2711\0");
      compatible = GpioSystem.devicetreeCompatible();
    }
    assertEquals(2, compatible.size());
    assertTrue(compatible.contains("raspberrypi,4-model-b"));
  }

}
