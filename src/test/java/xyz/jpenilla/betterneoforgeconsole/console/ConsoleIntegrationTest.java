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

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.jline.reader.Candidate;
import org.jline.reader.CompletingParsedLine;
import org.jline.reader.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import xyz.jpenilla.betterneoforgeconsole.configuration.Config;
import xyz.jpenilla.betterneoforgeconsole.util.ComponentAnsiSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(EphemeralTestServerProvider.class)
class ConsoleIntegrationTest {
  @Test
  void completesCommandWithLeadingSlash(final MinecraftServer server) {
    final MinecraftConsoleParser parser = new MinecraftConsoleParser(server);
    final List<Candidate> candidates = new ArrayList<>();
    new MinecraftCommandCompleter(server).complete(null, parser.parse("/he", 3, Parser.ParseContext.COMPLETE), candidates);
    assertTrue(candidates.stream().anyMatch(candidate -> candidate.value().equals("help")));
  }

  @Test
  void preservesSelectorSyntaxDuringCompletion(final MinecraftServer server) {
    final String input = "execute as @e[ty";
    final MinecraftConsoleParser parser = new MinecraftConsoleParser(server);
    final CompletingParsedLine line = (CompletingParsedLine) parser.parse(input, input.length(), Parser.ParseContext.COMPLETE);
    final List<Candidate> candidates = new ArrayList<>();
    new MinecraftCommandCompleter(server).complete(null, line, candidates);
    assertTrue(candidates.stream().anyMatch(candidate -> candidate.value().equals("@e[type=")));
    assertEquals("@e[type=minecraft:pig]", line.escape("@e[type=minecraft:pig]", false).toString());
    assertEquals(5, line.rawWordCursor());
  }

  @Test
  void preservesQuotedBrigadierArguments(final MinecraftServer server) {
    final String input = "team add test \"Hello world\"";
    final MinecraftConsoleParser parser = new MinecraftConsoleParser(server);
    final CompletingParsedLine line = (CompletingParsedLine) parser.parse(input, input.length(), Parser.ParseContext.COMPLETE);
    assertEquals("\"Hello world\"", line.word());
    assertEquals("\"Other name\"", line.escape("\"Other name\"", false).toString());
  }

  @Test
  void highlightsArgumentsAndInvalidCommands(final MinecraftServer server) {
    final MinecraftCommandHighlighter highlighter = new MinecraftCommandHighlighter(server, new Config().highlightColors());
    assertEquals("give @s minecraft:stone", highlighter.highlight(null, "give @s minecraft:stone").toString());
    assertTrue(highlighter.highlight(null, "give @s minecraft:stone").toAnsi().contains("\u001b["));
    assertTrue(highlighter.highlight(null, "not_a_command").toAnsi().contains("31m"));
  }

  @Test
  void resolvesRegisteredLogPatternAfterModLoading() {
    final PatternLayout layout = PatternLayout.newBuilder().withPattern(new Config().logPattern()).build();
    final Log4jLogEvent event = Log4jLogEvent.newBuilder().setLevel(Level.INFO)
      .setLoggerName("console-test").setMessage(new SimpleMessage("hello console")).build();
    final String output = layout.toSerializable(event);
    assertTrue(output.contains("hello console"));
    assertFalse(output.contains("aperMinecraftFormatting"));
  }

  @Test
  void resolvesTranslationArgumentsBeforeAnsiSerialization(final MinecraftServer server) {
    final Component component = Component.translatable("commands.seed.success", Component.literal("42").withStyle(ChatFormatting.GOLD));
    final String plain = org.jline.utils.AttributedString.stripAnsi(ComponentAnsiSerializer.serialize(component));
    assertEquals(component.getString(), plain);
    assertTrue(plain.contains("42"));
    assertFalse(plain.contains("commands.seed.success"));
  }
}
