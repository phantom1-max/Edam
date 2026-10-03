import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
    id("org.jetbrains.compose") version "1.7.3"
}

// Read editable version configuration from ../release-config.env
val releaseProps = Properties().apply {
    val configFile = rootProject.file("../release-config.env")
    if (configFile.exists()) {
        configFile.inputStream().use { load(it) }
    }
}

val edamVersionName: String = releaseProps.getProperty("APP_VERSION_NAME", "1.2.0")
    .removePrefix("v")
    .trim()
    .ifBlank { "1.2.0" }

group = "com.aistudio.edamlearn"
version = edamVersionName

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "com.edam.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )
            packageName = "Edam"
            packageVersion = edamVersionName
            description = "Edam — Interactive AI Learning Companion for Windows, macOS & Linux"
            vendor = "Edam Learning Studio"

            windows {
                menuGroup = "Edam"
                shortcut = true
            }
            macOS {
                bundleID = "com.aistudio.edamlearn.desktop"
            }
            linux {
                shortcut = true
            }
        }
    }
}
