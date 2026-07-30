import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "dev.banglaleaderboard"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://nexus.luckperms.net/repository/releases/")
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")

    // Vault
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // LuckPerms
    compileOnly("net.luckperms:api:5.4")

    // EssentialsX
    compileOnly("net.essentialsx:EssentialsX:2.21.0")

    // SnakeYAML (bundled with Paper, but explicit for IDE)
    compileOnly("org.yaml:snakeyaml:2.2")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    withType<ShadowJar> {
        archiveClassifier.set("")
        archiveFileName.set("BanglaLeaderboard-${version}.jar")
        destinationDirectory.set(file("$rootDir/build/libs"))
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
