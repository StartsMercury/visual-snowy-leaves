object Constants {
    const val VERSION = "0.4.1"

    const val VERSION_JAVA = 21
    const val VERSION_MINECRAFT = "1.21.3"
}

plugins {
    id("fabric-loom") version "1.8.12"
}

base {
    group = "io.github.startsmercury.visual_snowy_leaves"
    archivesName = "visual-snowy-leaves"
    version = createVersionString()
}

java {
    withSourcesJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(Constants.VERSION_JAVA)
    }
}

loom {
    accessWidenerPath = file("src/client/resources/visual-snowy-leaves.accesswidener")
    runtimeOnlyLog4j = true
    splitEnvironmentSourceSets()

    mods.register("visual-snowy-leaves") {
        sourceSet("main")
        sourceSet("client")
    }
}

repositories {
    maven {
        name = "Terraformers Maven"
        url = uri("https://maven.terraformersmc.com")
        content {
            includeGroup("com.terraformersmc")
        }
    }

    maven {
        name = "Modrinth Maven"
	    url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Constants.VERSION_MINECRAFT}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.9")

    modImplementation(fabricApi.module("fabric-resource-loader-v0", "0.109.0+1.21.3"))

    modImplementation("com.terraformersmc:modmenu:12.0.0-beta.1")
    modRuntimeOnly(fabricApi.module("fabric-screen-api-v1", "0.109.0+1.21.3"))
    modRuntimeOnly(fabricApi.module("fabric-key-binding-api-v1", "0.109.0+1.21.3"))
    modRuntimeOnly(fabricApi.module("fabric-lifecycle-events-v1", "0.109.0+1.21.3"))

    modCompileOnly("maven.modrinth:sodium:mc1.21.3-0.6.0-fabric")
}

tasks.withType<ProcessResources> {
    val data = mapOf(
        "version" to Constants.VERSION,
        "version_java" to Constants.VERSION_JAVA,
        "version_minecraft" to Constants.VERSION_MINECRAFT,
    )

    inputs.properties(data)

    filesMatching("fabric.mod.json") {
        expand(data)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = Constants.VERSION_JAVA
}

fun createVersionString(): String {
    val builder = StringBuilder()

    val isReleaseBuild = project.hasProperty("build.release")
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    if (isReleaseBuild) {
        builder.append(Constants.VERSION)
    } else {
        builder.append(Constants.VERSION.substringBefore('-'))
        builder.append("-snapshot")
    }

    builder.append("+mc").append(Constants.VERSION_MINECRAFT)

    if (!isReleaseBuild) {
        if (buildId != null) {
            builder.append("-build.${buildId}")
        } else {
            builder.append("-local")
        }
    }

    return builder.toString()
}
