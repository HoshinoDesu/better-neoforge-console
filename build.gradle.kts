import me.modmuss50.mpp.ReleaseType

plugins {
  val indraVersion = "3.1.3"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("net.neoforged.moddev") version "2.0.141"
  id("me.modmuss50.mod-publish-plugin") version "0.7.4"
}

version = "1.2.0"
group = "xyz.jpenilla"
description = "Server-side NeoForge mod enhancing the console with tab completions, colored log output, command syntax highlighting, command history, and more."

val minecraftVersion = "1.21.1"
val neoForgeVersion = "21.1.228"
val modId = "better_neoforge_console"
val modName = "Better NeoForge Console"
val githubUrl = "https://github.com/jpenilla/better-fabric-console"

neoForge {
  version = neoForgeVersion

  runs {
    register("server") {
      server()
    }
  }

  mods {
    register(modId) {
      sourceSet(sourceSets.main.get())
    }
  }
}

dependencies {
  annotationProcessor("org.apache.logging.log4j", "log4j-core", "2.24.1")

  val jlineVersion = "3.27.0"
  implementation("org.jline", "jline", jlineVersion)
  implementation("org.jline", "jline-terminal-jansi", jlineVersion)
  add("additionalRuntimeClasspath", "org.jline:jline:$jlineVersion")
  add("additionalRuntimeClasspath", "org.jline:jline-terminal-jansi:$jlineVersion")
  jarJar("org.jline:jline:$jlineVersion")
  jarJar("org.jline:jline-terminal-jansi:$jlineVersion")
  jarJar("org.jline:jline-terminal:$jlineVersion")
  jarJar("org.jline:jline-native:$jlineVersion")

  implementation("org.fusesource.jansi", "jansi", "2.4.1")
  add("additionalRuntimeClasspath", "org.fusesource.jansi:jansi:2.4.1")
  jarJar("org.fusesource.jansi:jansi:2.4.1")

  val adventureVersion = "4.17.0"
  implementation("net.kyori", "adventure-api", adventureVersion)
  implementation("net.kyori", "ansi", "1.0.3")
  add("additionalRuntimeClasspath", "net.kyori:adventure-api:$adventureVersion")
  add("additionalRuntimeClasspath", "net.kyori:ansi:1.0.3")
  jarJar("net.kyori:adventure-api:$adventureVersion")
  jarJar("net.kyori:adventure-key:$adventureVersion")
  jarJar("net.kyori:ansi:1.0.3")
  jarJar("net.kyori:examination-api:1.3.0")
  jarJar("net.kyori:examination-string:1.3.0")

  implementation("org.spongepowered:configurate-hocon:4.1.2")
  add("additionalRuntimeClasspath", "org.spongepowered:configurate-hocon:4.1.2")
  jarJar("org.spongepowered:configurate-hocon:4.1.2")
  jarJar("org.spongepowered:configurate-core:4.1.2")
  jarJar("com.typesafe:config:1.4.1")
  jarJar("io.leangen.geantyref:geantyref:1.3.11")

  compileOnly("org.checkerframework", "checker-qual", "3.48.1")
}

indra {
  javaVersions().target(21)
}

license {
  exclude("io/papermc/**")
}

tasks {
  processResources {
    val props = mapOf(
      "modId" to modId,
      "name" to modName,
      "description" to project.description,
      "version" to project.version,
      "githubUrl" to githubUrl,
      "minecraftVersionRange" to "[1.21.1,1.21.2)",
      "neoForgeVersionRange" to "[21.1,)"
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
}

publishMods.modrinth {
  projectId = "Y8o1j1Sf"
  type = ReleaseType.STABLE
  file = tasks.jar.flatMap { it.archiveFile }
  changelog = providers.environmentVariable("RELEASE_NOTES")
  accessToken = providers.environmentVariable("MODRINTH_TOKEN")
  minecraftVersions.add(minecraftVersion)
  modLoaders.add("neoforge")
}
