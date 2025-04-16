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

import static org.junit.jupiter.api.Assertions.*;

class Tm1638Test {

  @Test
  void print() {
    Tm1638 tm1638 = new Tm1638(null, null, null);
    tm1638.print(0, -1, "01011010", 1);
    assertArrayEquals(new boolean[]{false, true, false, true, true, false, true, false}, tm1638.led);
    tm1638.print(0, 0, "\uE100\uE102\uE104\uE1080248.", 1);
    assertArrayEquals(new byte[]{0, 2, 4, 8, 0x3F, 0x5B, 0x66, (byte) 0xFF}, tm1638.digit);
  }

}
