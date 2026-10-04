# Better NeoForge Console

A server-side NeoForge port of [Better Fabric Console](https://github.com/jpenilla/better-fabric-console), with Brigadier command completion, syntax highlighting, colored output, persistent history, and optional logging of player commands.

## Requirements

- Minecraft 1.21.11
- NeoForge 21.11.45 or a later 21.11 build
- Java 21

The `compat/minecraft-1.21.11` branch targets Minecraft 1.21.11; `master` targets Minecraft 26.3. Install the Jar matching your server version.

## Installation

Put `better-neoforge-console-mc1.21.11-1.3.0.jar` in the server's `mods` folder and start the server with `--nogui`. Configuration is generated at `config/better_neoforge_console.conf`. Command history is saved to `.console_history` in the server directory.

The mod uses the terminal provided by NeoForge. Interactive terminals support completion, highlighting, history navigation, and application-mode numpad digits. Redirected input and non-interactive server panels use plain command input; reaching the end of that input does not stop the server. In an interactive terminal, Ctrl+C or Ctrl+D requests a normal server shutdown.

NeoForge's terminal overrides are respected: `-Dterminal.jline=false` disables interactive input, and `-Dterminal.ansi=false` disables colored output. A panel must provide a real terminal for interactive features to work.

## Configuration

The existing configuration file and settings are preserved:

- `log-pattern`: Log4j console layout.
- `highlight-colors`: argument colors in order.
- `log-player-executed-commands`: whether to log player commands.

Restart after changing these settings. `/better-neoforge-console` displays the installed version.

## Building

Run `./gradlew build` with Java 21. The installable Jar is written to `build/libs/`. CI builds the same artifact and runs the console integration checks.

This port incorporates the upstream numpad fix (`c9d58f7`) and MinecraftServer system-message interception (`ebac8b4`), and adapts upstream terminal detection, Brigadier parsing, and tooltip coloring for NeoForge. It uses NeoForge's JLine runtime so the loader and mod share one terminal.

Licensed under MIT; see [license.txt](license.txt). Original project by Jason Penilla.
