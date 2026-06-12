/*
 * This file is part of Better NeoForge Console, licensed under the MIT License.
 *
 * Copyright (c) 2021-2024 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.betterneoforgeconsole.util;

import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jline.terminal.Terminal;

@DefaultQualifier(NonNull.class)
public final class TerminalModeDetection {
  private static final boolean CONSOLE_INPUT_AVAILABLE = System.console() != null;
  private static final TerminalMode MODE = detectMode();

  private TerminalModeDetection() {
  }

  private static TerminalMode detectMode() {
    if (!CONSOLE_INPUT_AVAILABLE) {
      return TerminalMode.DUMB;
    }

    final @Nullable Terminal terminal = TerminalConsoleAppender.getTerminal();
    if (terminal == null || Terminal.TYPE_DUMB.equals(terminal.getType())) {
      return TerminalMode.DUMB;
    }
    return TerminalMode.INTERACTIVE;
  }

  public static TerminalMode mode() {
    return MODE;
  }

  public static boolean isDumb() {
    return MODE == TerminalMode.DUMB;
  }

  public static boolean isInteractive() {
    return MODE == TerminalMode.INTERACTIVE;
  }

  public static boolean hasConsoleInput() {
    return CONSOLE_INPUT_AVAILABLE;
  }
}
