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
package xyz.jpenilla.betterneoforgeconsole;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import io.papermc.paper.console.HexFormattingConverter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.dedicated.DedicatedServer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import xyz.jpenilla.betterneoforgeconsole.configuration.Config;
import xyz.jpenilla.betterneoforgeconsole.console.ConsoleSetup;
import xyz.jpenilla.betterneoforgeconsole.console.ConsoleState;
import xyz.jpenilla.betterneoforgeconsole.console.ConsoleThread;
import xyz.jpenilla.betterneoforgeconsole.console.MinecraftCommandCompleter;
import xyz.jpenilla.betterneoforgeconsole.console.MinecraftCommandHighlighter;
import xyz.jpenilla.betterneoforgeconsole.console.MinecraftConsoleParser;

import static net.minecraft.commands.Commands.literal;

@Mod(BetterNeoForgeConsole.MOD_ID)
@DefaultQualifier(NonNull.class)
public final class BetterNeoForgeConsole {
  public static final String MOD_ID = "better_neoforge_console";
  public static final Logger LOGGER = LogUtils.getLogger();
  private static final TextColor PINK = TextColor.fromRgb(0xFF79C6);
  private static @MonotonicNonNull BetterNeoForgeConsole INSTANCE;

  private final Config config;
  private final ConsoleState consoleState;

  public BetterNeoForgeConsole() {
    INSTANCE = this;
    try {
      loadPluginsFromClassLoader(HexFormattingConverter.class.getClassLoader());
    } catch (final ReflectiveOperationException e) {
      LOGGER.error("Failed to load extra Log4j2 plugins", e);
    }

    this.config = this.loadModConfig();
    LOGGER.info("Initializing Better NeoForge Console...");
    this.consoleState = ConsoleSetup.init(this.config);
    NeoForge.EVENT_BUS.addListener(this::registerCommands);
    NeoForge.EVENT_BUS.addListener(this::onServerStarting);
  }

  private Config loadModConfig() {
    final Path configFile = FMLPaths.CONFIGDIR.get().resolve(MOD_ID + ".conf");
    final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
      .path(configFile)
      .build();
    try {
      if (!Files.exists(configFile.getParent())) {
        Files.createDirectories(configFile.getParent());
      }
      final CommentedConfigurationNode load = loader.load();
      final Config loadedConfig = load.get(Config.class);
      loader.save(loader.createNode(node -> node.set(loadedConfig)));
      return loadedConfig;
    } catch (final IOException ex) {
      throw new RuntimeException("Failed to load config", ex);
    }
  }

  private void onServerStarting(final ServerStartingEvent event) {
    this.initConsoleThread((DedicatedServer) event.getServer());
  }

  private void initConsoleThread(final DedicatedServer server) {
    this.consoleState.completer().delegateTo(new MinecraftCommandCompleter(server));
    this.consoleState.highlighter().delegateTo(new MinecraftCommandHighlighter(server, this.config.highlightColors()));
    this.consoleState.parser().delegateTo(new MinecraftConsoleParser(server));
    final ConsoleThread consoleThread = new ConsoleThread(server, this.consoleState.lineReader());
    consoleThread.setDaemon(true);
    consoleThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
    consoleThread.start();
  }

  private void registerCommands(final RegisterCommandsEvent event) {
    event.getDispatcher().register(literal("better-neoforge-console")
      .requires(stack -> stack.hasPermission(stack.getServer().getOperatorUserPermissionLevel()))
      .executes(this::executeCommand));
  }

  private int executeCommand(final CommandContext<CommandSourceStack> ctx) {
    final Component message = Component.empty()
      .append(Component.literal("Better NeoForge Console").withStyle(style -> style.withColor(PINK).withBold(true)))
      .append(Component.literal(" v").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
      .append(Component.literal(this.modVersion()).withStyle(ChatFormatting.GRAY));
    ctx.getSource().sendSystemMessage(message);
    return Command.SINGLE_SUCCESS;
  }

  private String modVersion() {
    return ModList.get().getModContainerById(MOD_ID)
      .map(container -> container.getModInfo().getVersion().toString())
      .orElse("unknown");
  }

  public Config config() {
    return this.config;
  }

  @SuppressWarnings("unchecked")
  private static void loadPluginsFromClassLoader(final ClassLoader loader) throws ReflectiveOperationException {
    final PluginRegistry registry = PluginRegistry.getInstance();
    final Method decodeCacheFiles = PluginRegistry.class.getDeclaredMethod("decodeCacheFiles", ClassLoader.class);
    decodeCacheFiles.setAccessible(true);
    final Map<String, List<PluginType<?>>> newPlugins =
      (Map<String, List<PluginType<?>>>) decodeCacheFiles.invoke(registry, loader);
    final Map<String, List<PluginType<?>>> pluginsByCategory = registry.loadFromMainClassLoader();
    newPlugins.forEach((category, discoveredPlugins) -> {
      final List<PluginType<?>> forCategory = pluginsByCategory.computeIfAbsent(category, c -> discoveredPlugins);
      if (forCategory == discoveredPlugins) {
        return;
      }
      for (final PluginType<?> pluginType : discoveredPlugins) {
        if (!forCategory.contains(pluginType)) {
          forCategory.add(pluginType);
        }
      }
    });
  }

  public static BetterNeoForgeConsole instance() {
    if (INSTANCE == null) {
      throw new IllegalStateException("Better NeoForge Console has not yet been initialized!");
    }
    return INSTANCE;
  }
}
