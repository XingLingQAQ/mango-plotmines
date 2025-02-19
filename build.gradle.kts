plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"

    val indraVersion = "3.1.3"
    id("net.kyori.indra") version indraVersion
    id("net.kyori.indra.git") version indraVersion

    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
}

group = "com.lukemango"
version = "1.0.6".decorateVersion()
description = "PlotMines for PlotSquared"

bukkitPluginYaml {
    main = "com.lukemango.plotmines.PlotMines"
    apiVersion = "1.18"
    website = "https://github.com/lukemango/mango-plotmines"
    authors = listOf("lukemango")
    softDepend = listOf("FastAsyncWorldEdit", "PlotSquared", "DecentHolograms")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven ("https://jitpack.io")
    maven ("https://repo.papermc.io/repository/maven-public/")
    maven ("https://oss.sonatype.org/content/groups/public/")
    maven ("https://repo.mattstudios.me/artifactory/public/") // TriumphGui
    maven ("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/") // PlugMan
}

dependencies {
    // Spigot
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")

    // Cloud
    implementation("org.incendo:cloud-paper:2.0.0-beta.8")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.8")
    implementation("org.incendo:cloud-annotations:2.0.0-rc.2")

    // FastAsyncWorldEdit
    implementation(platform("com.intellectualsites.bom:bom-newest:1.44")) // Also used for PlotSquared
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }

    // PlotSquared
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit")

    // Adventure
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    // TriumphGui
    implementation("dev.triumphteam:triumph-gui:3.1.7") { exclude("net.kyori") }

    // DecentHolograms
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.6")

    // PlugMan
    compileOnly("com.rylinaux:PlugMan:2.3.3")

    // bStats
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

indra {
    javaVersions().target(17)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    jar {
        archiveClassifier.set("noshade")
    }
    shadowJar {
        archiveFileName.set("${project.name}.jar") //-${project.version}
        sequenceOf(
            "org.incendo",
            "dev.triumphteam",
            "net.kyori",
            "io.papermc.lib",
            "io.leangen.geantyref",
            "org.bstats"
        ).forEach {
            relocate(it, "com.lukemango.plotmines.lib.$it")
        }
    }
    processResources {
        val tokens = mapOf(
            "project.version" to project.version
        )
        inputs.properties(tokens)
        filesMatching("**/*.yml") {
            // Some of our files are too large to use Groovy templating
            filter { string ->
                var result = string
                for ((key, value) in tokens) {
                    result = result.replace("\${$key}", value.toString())
                }
                result
            }
        }
    }
    compileJava {
        options.compilerArgs.add("-Xlint:-classfile,-processing")
    }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
    ?: error("Could not determine commit hash")

fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this
