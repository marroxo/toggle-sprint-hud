plugins {
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

version = "${property("mod_version")}+mc${property("minecraft_version")}"
group = property("maven_group")!!

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    maven("https://maven.terraformersmc.com") { name = "Terraformers" }
    mavenCentral()
}

dependencies {
    // String-invoked configuration names: Loom's type-safe Kotlin accessors aren't generated for the
    // SNAPSHOT plugin marker, so call the configurations by name directly (equivalent, always resolves).
    "minecraft"("com.mojang:minecraft:${property("minecraft_version")}")
    // No mappings() call and plain implementation/compileOnly configs: MC 26.2 ships de-obfuscated, so
    // Loom does no remapping and drops the mod* configurations (same model as Meteor's build script).
    "implementation"("net.fabricmc:fabric-loader:${property("loader_version")}")
    "implementation"("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    // Mod Menu is an optional runtime dependency; only needed at compile time for the config-screen entrypoint.
    "compileOnly"("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

tasks.processResources {
    val mcVersion = project.property("minecraft_version")!!
    val loaderVersion = project.property("loader_version")!!
    inputs.property("version", version)
    inputs.property("minecraft_version", mcVersion)
    inputs.property("loader_version", loaderVersion)
    filesMatching("fabric.mod.json") {
        expand(
            "version" to version,
            "minecraft_version" to mcVersion,
            "loader_version" to loaderVersion
        )
    }
}
