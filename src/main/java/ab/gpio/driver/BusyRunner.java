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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BusyRunner implements AutoCloseable {

  private final Set<BusyRunnable> runnables = new CopyOnWriteArraySet<>();
  private boolean open;

  public void add(BusyRunnable r) {
    runnables.add(r);
  }

  public void remove(BusyRunnable r) {
    runnables.remove(r);
  }

  protected void run() {
    while (open) for (BusyRunnable r : runnables) r.run();
  }

  public BusyRunner open() {
    if (open) throw new IllegalStateException();
    open = true;
    new Thread(this::run).start();
    return this;
  }

  @Override
  public void close() {
    open = false;
  }

}
