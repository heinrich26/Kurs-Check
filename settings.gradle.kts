plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}

rootProject.name = "Kurs-Check"
include("app", "lib", "merger", "resource-generator")