plugins {
    id("myproject.java-conventions")
    id("application")
}

val javaMainClass = "SvgProcessorKt"
val appLayout = rootProject.project("app").layout

tasks.register<JavaExec>("processVectors") {
    jvmArgs("-Duser.language=en", "-Duser.country=US")
    args(appLayout.projectDirectory.dir("assets"), appLayout.projectDirectory.dir("src/main/resources"), appLayout.buildDirectory.dir("generated/sources/vectors/main").get())
    group = "Execution"
    description = "Erstellt Shape Objekte für alle .svg Dateien"
    mainClass = javaMainClass
    classpath = sourceSets["main"].runtimeClasspath
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.squareup.kotlinpoet)

    testImplementation(libs.junit.jupiter)
}