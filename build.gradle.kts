object Constants {
    const val VERSION = "0.6.0-beta.5"

    const val VERSION_JAVA = 25
    const val VERSION_MINECRAFT = "26.1-snapshot-2"
    const val VERSION_FAPI = "0.141.2+26.1"
}

plugins {
    id("net.fabricmc.fabric-loom") version "1.14.10"
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
        name = "caffeinemcRepositoryReleases"
        url = uri("https://maven.caffeinemc.net/releases")
        content {
            includeGroup("net.caffeinemc")
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

// TODO separate compat test runs instead of manually changing deps...
dependencies {
    minecraft("com.mojang:minecraft:${Constants.VERSION_MINECRAFT}")
    implementation("net.fabricmc:fabric-loader:0.18.4")

    // Optional: Custom Key Mappings
    implementation(fabricApi.module("fabric-lifecycle-events-v1", Constants.VERSION_FAPI))
    implementation(fabricApi.module("fabric-key-mapping-api-v1", Constants.VERSION_FAPI))

    // Mod Menu Support
    implementation("maven.modrinth:modmenu:18.0.0-alpha.3")
    runtimeOnly(fabricApi.module("fabric-resource-loader-v0", Constants.VERSION_FAPI))
    runtimeOnly(fabricApi.module("fabric-screen-api-v1", Constants.VERSION_FAPI))

    // Fabric Renderer Indigo Support
    compileOnly(fabricApi.module("fabric-rendering-v1", Constants.VERSION_FAPI))
    compileOnly(fabricApi.module("fabric-renderer-indigo", Constants.VERSION_FAPI))

    // Sodium's Renderer Support
    // compileOnly("net.caffeinemc:sodium-fabric:0.8.2+mc1.21.11")

    // Iris (Without) Shaders Support
    // compileOnly("maven.modrinth:iris:1.10.4+1.21.11-fabric")
}

tasks.withType<ProcessResources> {
    val data = mapOf(
        "version" to Constants.VERSION,
        "version_java" to Constants.VERSION_JAVA,
        "version_game" to "26.1-alpha.2",
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

loom {
    runConfigs {
        val client by existing {
            vmArg("-Dmixin.debug.export=true")
        }
    }
}
