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

import ab.tui.Tui;

import java.awt.Dimension;
import java.util.function.Consumer;

public class Max7219 implements Tui {

  //public static final int T_CP_NS = 100; // T_CH_NS + T_CL_NS
  public static final int T_CH_NS = 50;
  public static final int T_CL_NS = 50;
  public static final int T_CSS_NS = 25;
  public static final int T_CSH_NS = 0;
  public static final int T_DS_NS = 25;
  public static final int T_DH_NS = 0;
  public static final int T_LDCK_NS = 50;
  public static final int T_CSW_NS = 50;
  private final Pin din;
  private final Pin cs;
  private final Pin clk;
  private final boolean[][] image;
  private boolean open;

  public Max7219(Pin din, Pin cs, Pin clk) {
    this.din = din;
    this.cs = cs;
    this.clk = clk;
    this.image = new boolean[8][32];
  }

  @Override
  public Dimension getSize() {
    return new Dimension(32, 8);
  }

  /**
   * @param s '0 .' off '1%#' on
   * @param attr any
   */
  @Override
  public void print(int x, int y, String s, int attr) {
    for (char c : s.toCharArray()) image[y][x++] = (c & 1) > 0;
  }

  @Override
  public void update() {
    short[] data = new short[4];
    for (int y = 0; y < 8; y++) {
      for (int i = 0; i < 4; i++) {
        short d = (short) (y + 1 << 8);
        for (int x = 0; x < 8; x++) {
          if (image[y][i * 8 + x]) d |= 0x80 >> x;
        }
        data[i] = d;
      }
      write(data);
    }
  }

  protected void writeAll(int data) {
    write(new short[]{(short) data, (short) data, (short) data, (short) data});
  }

  synchronized protected void write(short[] data) {
    if (!open) throw new IllegalStateException("closed");
    clk.set(false);
    cs.set(false);
    sleep(T_CSS_NS);
    for (short d : data) {
      for (int i = 0; i < 16; i++) {
        din.set(d < 0);
        sleep(Math.max(T_DS_NS, T_CL_NS));
        clk.set(true);
        sleep(Math.max(T_DH_NS, T_CH_NS));
        clk.set(false);
        d <<= 1;
      }
    }
    sleep(T_CSH_NS);
    cs.set(true);
    sleep(Math.max(T_CSW_NS, T_LDCK_NS));
  }

  protected void sleep(int ns) {
    long time = System.nanoTime() + ns;
    while (System.nanoTime() <= time) ;
  }

  /**
   * @param brightness 0-15
   */
  public void setBrightness(int brightness) {
    writeAll(0x0A00 + Math.min(Math.max(0, brightness), 15));
  }

  @Override
  public void setKeyListener(Consumer<String> keyListener) {
    // no keyboard, do nothing
  }

  @Override
  public Max7219 open() {
    if (open) throw new IllegalStateException("not closed");
    open = true;
    din.open();
    cs.open();
    clk.open();
    writeAll(0x0C00); // screen off
    writeAll(0x0F00); // test off
    setBrightness(0); // brightness min
    writeAll(0x0900); // no decode
    writeAll(0x0B07); // all 8 digits/lines
    for (byte i = 1; i <= 8; i++) writeAll(0x100 * i);
    writeAll(0x0C01); // screen on
    return this;
  }

  @Override
  public void close() {
    if (open) writeAll(0x0C00); // screen off
    open = false;
    din.close();
    cs.close();
    clk.close();
  }

  public static void main(String[] args) {
    if (args.length != 6) {
      System.out.println("java -cp .jar ab.gpio.Max7219 0 10 0 9 0 11");
      System.exit(1);
    }
    Pin din = new Pin(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    Pin cs = new Pin(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    Pin clk = new Pin(Integer.parseInt(args[4]), Integer.parseInt(args[5]));
    try (Max7219 max7219 = new Max7219(din, cs, clk).open()) {
      max7219.setBrightness(15);
      max7219.print(0, 0, "................................", 0);
      max7219.print(0, 1, "#.#.....#.#......#.#.........#..", 0);
      max7219.print(0, 2, "#.#.###.#.#..##..#.#..##.##..#.#", 0);
      max7219.print(0, 3, "###.#...#.#.#.#..#.#.#.#.#.#.#.#", 0);
      max7219.print(0, 4, "#.#.###.#.#.#.#..#.#.#.#.##..#.#", 0);
      max7219.print(0, 5, "#.#.#...#.#.#.#..###.#.#.#.#.#.#", 0);
      max7219.print(0, 6, "#.#..##.#.#.##...###.##..#.#.#.#", 0);
      max7219.print(0, 7, "................................", 0);
      max7219.update();
      Thread.sleep(1000);
      max7219.setBrightness(0);
      max7219.update();
    } catch (InterruptedException ignore) {}
  }
}
