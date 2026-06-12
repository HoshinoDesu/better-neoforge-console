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

import java.io.IOException;
import java.nio.file.Paths;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import xyz.jpenilla.betterneoforgeconsole.configuration.Config;

@DefaultQualifier(NonNull.class)
public final class ConsoleSetup {
  private ConsoleSetup() {
  }

  private static LineReader buildLineReader(
    final @Nullable Terminal terminal,
    final Completer completer,
    final Highlighter highlighter,
    final Parser parser
  ) {
    System.setProperty("org.jline.reader.support.parsedline", "true"); // to hide a warning message about the parser not supporting

    final LineReaderBuilder builder = LineReaderBuilder.builder()
      .appName("Dedicated Server")
      .variable(LineReader.HISTORY_FILE, Paths.get(".console_history"))
      .completer(completer)
      .highlighter(highlighter)
      .parser(parser)
      .completionMatcher(new MinecraftCompletionMatcher())
      .option(LineReader.Option.INSERT_TAB, false)
      .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
      .option(LineReader.Option.COMPLETE_IN_WORD, true);
    if (terminal != null) {
      builder.terminal(terminal);
    } else {
      // No JLine terminal from TerminalConsoleAppender; use an explicitly dumb
      // terminal to avoid JLine printing a warning when it falls back on its own.
      try {
        builder.terminal(TerminalBuilder.builder().dumb(true).build());
      } catch (final IOException ignore) {
        // let LineReaderBuilder create its own fallback terminal
      }
    }
    return builder.build();
  }

  public static ConsoleState init(final Config config) {
    final DelegatingCompleter delegatingCompleter = new DelegatingCompleter();
    final DelegatingHighlighter delegatingHighlighter = new DelegatingHighlighter();
    final DelegatingParser delegatingParser = new DelegatingParser();
    final @Nullable Terminal terminal = TerminalConsoleAppender.getTerminal();
    final LineReader lineReader = buildLineReader(
      terminal,
      delegatingCompleter,
      delegatingHighlighter,
      delegatingParser
    );

    if (terminal != null) {
      TerminalConsoleAppender.setReader(lineReader);
    }

    final ConsoleAppender consoleAppender = new ConsoleAppender(
      lineReader,
      config.logPattern(),
      null
    );
    consoleAppender.start();

    final Logger logger = (Logger) LogManager.getRootLogger();
    final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    final LoggerConfig loggerConfig = loggerContext.getConfiguration().getLoggerConfig(logger.getName());

    loggerConfig.removeAppender("SysOut");
    loggerConfig.removeAppender("Console");
    loggerConfig.removeAppender("TerminalConsole");
    loggerConfig.addAppender(consoleAppender, Level.INFO, null);
    loggerContext.updateLoggers();

    return new ConsoleState(lineReader, delegatingCompleter, delegatingHighlighter, delegatingParser);
  }
}
