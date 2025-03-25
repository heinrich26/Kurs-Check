/*
 * Copyright (c) 2022-2025  Hendrik Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI
import java.nio.file.Files


plugins {
    kotlin("jvm")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platformFromCatalog("kotlin-bom"))
    implementationFromCatalog("kotlin-stdlib")
    implementationFromCatalog("kotlin-reflect")

    testImplementationFromCatalog("junit-jupiter")
    testImplementationFromCatalog("junit-jupiterParams")
}

group = "com.kurswahlApp"

val projectJavaVersion = "17"
val arch = providers.gradleProperty("arch").getOrElse("x64")


tasks.withType<JavaCompile>().configureEach {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(projectJavaVersion))
        if (arch == X86) vendor.set(JvmVendorSpec.AZUL)
    }
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(projectJavaVersion))
        if (arch == X86) vendor.set(JvmVendorSpec.AZUL)
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(projectJavaVersion))
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(projectJavaVersion))
        if (arch == X86) vendor.set(JvmVendorSpec.AZUL)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(projectJavaVersion))
        if (arch == X86) vendor.set(JvmVendorSpec.AZUL)
    }
}

tasks.test {
    useJUnitPlatform()
}

val resDirName by extra("installerResources")
val jlinkJvmModules by extra(
    listOf(
        "java.base", "java.datatransfer", "java.desktop", "java.logging",
        "java.management", "java.net.http", "java.prefs", "java.sql", "jdk.localedata",
        "java.transaction.xa", "java.xml", "jdk.crypto.cryptoki", "jdk.crypto.ec"
    )
)

tasks.register<Copy>("copyInstallerResources") {
    from("${rootProject.projectDir}/res/icons") {
        include("app_icon.ico", "app_icon.icns", "app_icon.png")
        rename("app_icon(.*)", "${rootProject.name}\$1")
    }
    from(layout.projectDirectory.dir("res"), "${rootProject.projectDir}/app/res")
    filter(ReplaceTokens::class, "tokens" to mapOf("platform" to arch))
    into(layout.buildDirectory.dir(resDirName))
}

tasks.register("downloadSchoolsData") {
    val f = layout.buildDirectory.file("resources/main/schools-data/per-school-settings.json").get().asFile

    doFirst {
        Files.createDirectories(f.parentFile.toPath())
        URI("https://raw.githubusercontent.com/heinrich26/Kurs-Check/data/per-school-settings.json")
            .toURL()
            .openStream()
            .use {
                i -> f.outputStream().use { i.copyTo(it) }
            }
    }
}