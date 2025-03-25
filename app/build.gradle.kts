import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings
import java.util.*

plugins {
    id("myproject.java-conventions")
    alias(libs.plugins.jpackage)
    alias(libs.plugins.ideaExt)
}


dependencies {
    implementation(project(":lib"))

    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.kotlinx.cli)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.lgooddatepicker)
}

kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("build/generated/sources/vectors/main")
    }
}

rootProject.idea.module.settings.packagePrefix["src/main/kotlin"] = group as String

application {
    mainClass = "${group}.gui.GuiMain"
}

val resDirName: String by extra

tasks.named("compileKotlin").configure {
    dependsOn(":resource-generator:processVectors")
}

val downloadSchoolsData by tasks.existing
val copyInstallerResources by tasks.existing
tasks.jpackageImage {
    dependsOn(downloadSchoolsData, copyInstallerResources)
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

    modules.set(jlinkJvmModules)

    jpackage {
        imageName = projectName
        imageOptions = listOf("--icon", "$rootDir/res/icons/app_icon.$iconExt")

        installerName = projectName
        installerOutputDir = file("$rootDir/out/executable/basic")
        packageType?.let { installerType = it } // baut alle möglichen Installertypen auf Linux
        installerOptions = listOf("--file-associations", layout.buildDirectory.file("$resDirName/kurswahl-${osPrefix}.properties").get().toString(),
            "--vendor", "\"Willi-Graf-Gymnasium\"",
            "--description", "\"$description\"",
            "--copyright", "\"Copyright Hendrik Horstmann ${Calendar.getInstance().get(Calendar.YEAR)}, Alle Rechte vorbehalten\"",
            "--about-url", "https://github.com/heinrich26/kurswahlApp",
            "--license-file", "$rootDir/COPYRIGHT",
            "@res/$osPrefix-opts.txt")

        resourceDir = layout.buildDirectory.dir(resDirName).get().asFile
    }
}