import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.serialization") version "1.9.20"
	id("dev.architectury.loom") version "1.3-SNAPSHOT"
}

version = "0.1.1"
group = "compass_system.wiziplicity"
base.archivesName = "wiziplicity"

loom {
	silentMojangMappingsLicense()

	runs.configureEach {
		isIdeConfigGenerated = false
	}
}

dependencies {
	minecraft (libs.minecraft)
	mappings(loom.officialMojangMappings())
	modImplementation(libs.fabric.loader)

	modImplementation(libs.fabric.api)
	modImplementation(libs.fabric.kotlin)
}

tasks {
	processResources {
		val properties = mutableMapOf("version" to project.version)
		inputs.properties(properties)

		filesMatching("fabric.mod.json") {
			expand(properties)
		}
	}

	withType<JavaCompile> {
		options.release = 17
	}

	withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "17"
		}
	}

	jar {
		from("license.md")
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}
