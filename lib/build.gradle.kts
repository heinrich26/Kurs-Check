import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

plugins {
    id("myproject.java-conventions")
    alias(libs.plugins.ideaExt)
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.apache.pdfbox)
}

project.idea.module.settings.packagePrefix["src/main/kotlin"] = "${group as String}.data"

val javaMainClass = "${group}.data.lusd_pdf.MainKt"

tasks.register<JavaExec>("extractLusdIds") {
    group = "Execution"
    description = "Run the main class with JavaExecTask"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = javaMainClass
}