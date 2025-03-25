import org.apache.tools.ant.taskdefs.condition.Os
import java.util.*

plugins {
    id("myproject.java-conventions")
    alias(libs.plugins.jpackage)
}

description = "Fügt mehrere Kurswahlen in eine CSV Tabelle zusammen!"

application {
    mainClass = "${group}.gui.GuiMain"
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":app"))

    implementation(libs.kotlinx.cli)
    implementation(libs.apache.commonsCSV)
}

val resDirName: String by extra

val downloadSchoolsData by tasks.existing
val copyInstallerResources by tasks.existing {
    dependsOn(downloadSchoolsData)
}

tasks.jpackageImage {
    dependsOn(copyInstallerResources)
}

// Der Compiler lässt uns nicht direkt entpacken, warum auch immer
val osSpec = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> listOf("win", "ico", "msi")
    Os.isFamily(Os.FAMILY_MAC) -> listOf("mac", "icns", "pkg")
    else -> listOf("linux", "png", null)
}
val (osPrefix, iconExt, packageType) = osSpec

val jlinkJvmModules: List<String> by extra

val projectName = rootProject.name

runtime {
    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")

    additive = true
    modules.set(jlinkJvmModules)

    jpackage {
        imageName = projectName
        imageOptions = listOf(
            "--add-launcher", "wahl-merger=res/$osPrefix-merger.properties",
            "--icon", "$rootDir/res/icons/app_icon.$iconExt"
        )

        installerName = "$projectName-full"
        installerOutputDir = file("$rootDir/out/executable/full")
        packageType?.let { installerType = it } // baut alle möglichen Installertypen auf Linux
        installerOptions = listOf("--file-associations", layout.buildDirectory.file("$resDirName/kurswahl-$osPrefix.properties").get().toString(),
            "--vendor", "\"Willi-Graf-Gymnasium\"",
            "--description", "\"$description\"",
            "--copyright", "\"Copyright Hendrik Horstmann ${Calendar.getInstance().get(Calendar.YEAR)}, Alle Rechte vorbehalten\"",
            "--about-url", "https://github.com/heinrich26/kurswahlApp",
            "--license-file", "$rootDir/COPYRIGHT",
            "@$rootDir/app/res/$osPrefix-opts.txt")
        resourceDir = layout.buildDirectory.dir(resDirName).get().asFile
    }
}