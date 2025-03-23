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

/**
 * The BusyRunnable interface should be implemented by any class intended to be executed in a busy loop.
 * The run() implementation must perform a single operation and return, allowing multiple
 * BusyRunnable objects to share one thread. For best results, the code should be short
 * and ensure that each execution takes approximately the same duration.
 */
public interface BusyRunnable extends Runnable, AutoCloseable {
  @Override
  void run();

  BusyRunnable open();

  @Override
  void close();
}
