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
package xyz.jpenilla.betterneoforgeconsole.console;

import java.nio.file.Paths;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Macro;
import org.jline.terminal.Terminal;
import xyz.jpenilla.betterneoforgeconsole.configuration.Config;

@DefaultQualifier(NonNull.class)
public final class ConsoleSetup {
  private ConsoleSetup() {
  }

  public static ConsoleState init(final Config config) {
    final DelegatingCompleter completer = new DelegatingCompleter();
    final DelegatingHighlighter highlighter = new DelegatingHighlighter();
    final DelegatingParser parser = new DelegatingParser();
    final @Nullable Terminal terminal = TerminalConsoleAppender.getTerminal();
    if (terminal == null) {
      return new ConsoleState(null, completer, highlighter, parser);
    }

    final LineReader lineReader = LineReaderBuilder.builder()
      .appName("Dedicated Server")
      .terminal(terminal)
      .variable(LineReader.HISTORY_FILE, Paths.get(".console_history"))
      .completer(completer)
      .highlighter(highlighter)
      .parser(parser)
      .completionMatcher(new MinecraftCompletionMatcher())
      .option(LineReader.Option.INSERT_TAB, false)
      .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
      .option(LineReader.Option.COMPLETE_IN_WORD, true)
      .build();

    // Upstream c9d58f7: application keypad mode sends escape sequences for digits.
    final KeyMap<Binding> keys = lineReader.getKeyMaps().get(LineReader.MAIN);
    for (int i = 0; i < 10; i++) {
      keys.bind(new Macro(Integer.toString(i)), "\033O" + (char) ('p' + i));
    }
    TerminalConsoleAppender.setReader(lineReader);

    final LoggerContext context = (LoggerContext) LogManager.getContext(false);
    final LoggerConfig logger = context.getConfiguration().getRootLogger();
    final ConsoleAppender appender = new ConsoleAppender(lineReader, config.logPattern());
    appender.start();
    logger.removeAppender("SysOut");
    logger.removeAppender("Console");
    logger.removeAppender("TerminalConsole");
    logger.addAppender(appender, Level.INFO, null);
    context.updateLoggers();
    return new ConsoleState(lineReader, completer, highlighter, parser);
  }
}
