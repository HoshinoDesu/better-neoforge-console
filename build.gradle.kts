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

val targetNeoForgeVersions = mapOf(
  "1.21.1" to "21.1.228",
  "1.21.2" to "21.2.1-beta",
  "1.21.3" to "21.3.96",
  "1.21.4" to "21.4.157",
  "1.21.5" to "21.5.97",
  "1.21.6" to "21.6.20-beta",
  "1.21.7" to "21.7.25-beta",
  "1.21.8" to "21.8.53",
  "1.21.9" to "21.9.16-beta",
  "1.21.10" to "21.10.64",
  "1.21.11" to "21.11.42"
)
val minecraftVersion = providers.gradleProperty("targetMinecraftVersion").orElse("1.21.1").get()
val neoForgeVersion = targetNeoForgeVersions[minecraftVersion]
  ?: throw GradleException("Unsupported targetMinecraftVersion '$minecraftVersion'. Supported versions: ${targetNeoForgeVersions.keys.joinToString()}")
val minecraftVersionRange = singlePatchRange(minecraftVersion)
val neoForgeVersionRange = neoForgeLineRange(neoForgeVersion)
val modId = "better_neoforge_console"
val modName = "Better NeoForge Console"
val githubUrl = "https://github.com/jpenilla/better-fabric-console"

fun singlePatchRange(version: String): String {
  val base = version.substringBeforeLast('.')
  val patch = version.substringAfterLast('.').toInt()
  return "[$version,$base.${patch + 1})"
}

fun neoForgeLineRange(version: String): String {
  val line = version.substringBeforeLast('.')
  val nextLine = "21.${line.substringAfter('.').toInt() + 1}"
  return "[$line,$nextLine)"
}

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
  fun addToAdditionalRuntimeClasspath(dependencyNotation: String) {
    if (minecraftVersion.substringAfterLast('.').toInt() < 9 && configurations.findByName("additionalRuntimeClasspath") != null) {
      add("additionalRuntimeClasspath", dependencyNotation)
    }
  }

  annotationProcessor("org.apache.logging.log4j", "log4j-core", "2.24.1")

  val jlineVersion = "3.27.0"
  compileOnly("org.jline", "jline-reader", jlineVersion)
  compileOnly("org.jline", "jline-terminal-jansi", jlineVersion)

  compileOnly("org.fusesource.jansi", "jansi", "2.4.1")

  val adventureVersion = "4.17.0"
  implementation("net.kyori", "adventure-api", adventureVersion)
  implementation("net.kyori", "ansi", "1.0.3")
  addToAdditionalRuntimeClasspath("net.kyori:adventure-api:$adventureVersion")
  addToAdditionalRuntimeClasspath("net.kyori:ansi:1.0.3")
  jarJar("net.kyori:adventure-api:$adventureVersion")
  jarJar("net.kyori:adventure-key:$adventureVersion")
  jarJar("net.kyori:ansi:1.0.3")
  jarJar("net.kyori:examination-api:1.3.0")
  jarJar("net.kyori:examination-string:1.3.0")

  implementation("org.spongepowered:configurate-hocon:4.1.2")
  addToAdditionalRuntimeClasspath("org.spongepowered:configurate-hocon:4.1.2")
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
      "minecraftVersionRange" to minecraftVersionRange,
      "neoForgeVersionRange" to neoForgeVersionRange
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
