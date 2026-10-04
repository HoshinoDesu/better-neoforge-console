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

import java.util.Optional;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.ansi.ColorLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ComponentAnsiSerializer {
  private ComponentAnsiSerializer() {
  }

  public static String serialize(final Component component) {
    final TextComponent.Builder text = net.kyori.adventure.text.Component.text();
    // Visit resolved text so Minecraft translations and their arguments retain their styles.
    component.visit((style, content) -> {
      final TextComponent.Builder part = net.kyori.adventure.text.Component.text().content(content)
        .decoration(TextDecoration.BOLD, style.isBold())
        .decoration(TextDecoration.ITALIC, style.isItalic())
        .decoration(TextDecoration.UNDERLINED, style.isUnderlined())
        .decoration(TextDecoration.STRIKETHROUGH, style.isStrikethrough())
        .decoration(TextDecoration.OBFUSCATED, style.isObfuscated());
      if (style.getColor() != null) {
        part.color(TextColor.color(style.getColor().getValue()));
      }
      text.append(part);
      return Optional.empty();
    }, Style.EMPTY);
    final ColorLevel level = TerminalConsoleAppender.isAnsiSupported() ? ColorLevel.compute() : ColorLevel.NONE;
    return ANSIComponentSerializer.builder().colorLevel(level).build().serialize(text.build());
  }
}
