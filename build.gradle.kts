plugins {
  java
  checkstyle
  id("net.neoforged.moddev") version "2.0.148"
}

version = "1.3.0"
group = "xyz.jpenilla"
description = "Server-side NeoForge mod enhancing the console with tab completions, colored log output, command syntax highlighting, command history, and more."

val minecraftVersion = "26.3"
val neoForgeVersion = "26.3.0.48-beta"
val modId = "better_neoforge_console"

neoForge {
  version = neoForgeVersion
  runs {
    register("server") {
      server()
      programArgument("--nogui")
    }
  }
  mods {
    register(modId) {
      sourceSet(sourceSets.main.get())
    }
  }
  unitTest {
    enable()
    testedMod = mods[modId]
  }
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(25)
  withSourcesJar()
}

checkstyle {
  toolVersion = "10.21.4"
  configDirectory = layout.projectDirectory.dir(".checkstyle")
  configProperties["configDirectory"] = configDirectory.get().asFile.absolutePath
}

dependencies {
  // Use the JLine API supplied by NeoForge's TerminalConsoleAppender.
  compileOnly("org.jline:jline-reader:3.20.0")

  val adventureVersion = "4.17.0"
  implementation("net.kyori:adventure-api:$adventureVersion")
  implementation("net.kyori:adventure-text-serializer-ansi:$adventureVersion")
  implementation("net.kyori:ansi:1.0.3")
  jarJar("net.kyori:adventure-api:$adventureVersion")
  jarJar("net.kyori:adventure-key:$adventureVersion")
  jarJar("net.kyori:adventure-text-serializer-ansi:$adventureVersion")
  jarJar("net.kyori:ansi:1.0.3")
  jarJar("net.kyori:examination-api:1.3.0")
  jarJar("net.kyori:examination-string:1.3.0")

  implementation("org.spongepowered:configurate-hocon:4.1.2")
  jarJar("org.spongepowered:configurate-hocon:4.1.2")
  jarJar("org.spongepowered:configurate-core:4.1.2")
  jarJar("com.typesafe:config:1.4.1")
  jarJar("io.leangen.geantyref:geantyref:1.3.11")

  compileOnly("org.checkerframework:checker-qual:3.48.1")
  testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
  testImplementation("net.neoforged:testframework:$neoForgeVersion")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
  test {
    useJUnitPlatform()
  }
  processResources {
    val props = mapOf(
      "modId" to modId,
      "name" to "Better NeoForge Console",
      "description" to project.description,
      "version" to project.version,
      "githubUrl" to "https://github.com/HoshinoDesu/better-neoforge-console",
      "minecraftVersionRange" to "[$minecraftVersion,26.4)",
      "neoForgeVersionRange" to "[$neoForgeVersion,26.4)"
    )
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") {
      expand(props)
    }
  }
  jar {
    from("license.txt")
    archiveFileName.set("better-neoforge-console-mc$minecraftVersion-${project.version}.jar")
  }
  withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
  }
}
