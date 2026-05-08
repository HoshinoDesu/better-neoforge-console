dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
  }
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.neoforged.net/releases")
    maven("https://repo.jpenilla.xyz/snapshots/")
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "better-neoforge-console"
