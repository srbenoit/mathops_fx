plugins {
    id("application")
    id("org.beryx.runtime") version "1.13.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "dev.mathops.fx"
version = "1.0"

repositories {
    mavenCentral()
}


javafx {
    version = "23.0.1"
    modules("javafx.controls", "javafx.swing", "javafx.media", "javafx.graphics", "javafx.web")
}

application {
    mainClass = "dev.mathops.fx.coursebuilder.Launcher"
}

runtime {
    options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    modules = listOf("java.base")

    jpackage {
        targetPlatformName = "win"

        // The app name
        imageName = "Course Builder"
        jvmArgs = listOf("-Djdk.gtk.version=2")

        resourceDir = file("package/")
        if (targetPlatformName == "mac") {
            targetPlatform("mac") {
                // Use downloaded Bellsoft Liberica JDK with JavaFX bundled!
                jdkHome = "/Volumes/Fastdrive/Development/JDKs/Bellsoft/mac/jdk-17.0.5-full.jdk"
                // We also could directly download a JDK
                //jdkHome = jdkDownload("https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_macos-x64_bin.tar.gz")
            }
            installerType = "pkg"
        }
        if (targetPlatformName == "win") {
            targetPlatform("win") {
                jdkHome = "C:/Program Files/Java/jdk-21"
            }
            installerType = "exe"
        }
        if (targetPlatformName == "linux") {
            targetPlatform("linux") {
            }
            installerType = "deb"
        }
        // Add jpackage-specific options
        installerOptions = listOf(
            "--name", "Course Builder", // installer name
            "--description", project.description,
            "--copyright", "Copyright 2025 Mathops.dev",
            "--vendor", "Mathops.dev"
        )
        if (installerType == "pkg") {
            imageOptions = imageOptions.plus(listOf("--icon", "src/main/resources/icon.icns"))
            installerOptions = installerOptions.plus(listOf(
                "--license-file", "package/LICENSE-OS-Installer.txt"
            ))
        }
        if (installerType == "exe") {
            imageOptions = imageOptions.plus(listOf("--icon", "src/main/resources/icon.ico"))
            installerOptions = installerOptions.plus(listOf(
                // "--win-per-user-install", // Install only for current user
                // "--win-console", // Shows what Java outputs to the console
                "--win-dir-chooser",
                "--win-menu", "--win-shortcut"
            ))
        }
        if (installerType in listOf("deb", "rpm")) {
            imageOptions = imageOptions.plus(listOf("--icon", "src/main/resources/icon_256x256.png"))
            installerOptions = installerOptions.plus(listOf(
                "--linux-menu-group", "Utility",
                "--linux-shortcut"
            ))
        }
        if (installerType == "deb") {
            installerOptions = installerOptions.plus(listOf(
                "--linux-deb-maintainer", "info@mathops.dev"
            ))
        }
        if (installerType == "rpm") {
            installerOptions = installerOptions.plus(listOf(
                "--linux-rpm-license-type", "GPLv3"
            ))
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(files("../../IDEA/mathops_commons/out/libs/mathops_commons.jar"))
    implementation(files("../../IDEA/mathops_text/out/libs/mathops_text.jar"))
    implementation(files("../../IDEA/mathops/out/libs/mathops.jar"))

    implementation("org.apache.pdfbox:pdfbox:3.0.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "dev.mathops.fx.coursebuilder.Launcher"
    }
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
