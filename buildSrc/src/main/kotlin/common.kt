

/*
 * Copyright (c) 2025  Hendrik Horstmann
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

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.plugins.ide.idea.model.IdeaModel

class VersionCatalogHelper(private val project: Project) {

    private val versionCatalog = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    private val logger: Logger = project.logger

    fun dependency(libName: String, configurationName: String) {
        versionCatalog.findLibrary(libName).ifPresentOrElse(
            { library ->
                project.dependencies.add(configurationName, library)
            },
            { logger.warn("Library '$libName' not found in version catalog.") }
        )
    }

    fun platform(libName: String): Provider<MinimalExternalModuleDependency> =
        versionCatalog
            .findLibrary(libName)
            .orElseThrow()
            .let(project.dependencies::platform)
}

// Extension functions for different configurations
fun Project.implementationFromCatalog(libName: String) {
    VersionCatalogHelper(this).dependency(libName, "implementation")
}

fun Project.testImplementationFromCatalog(libName: String) {
    VersionCatalogHelper(this).dependency(libName, "testImplementation")
}

fun Project.compileOnlyFromCatalog(libName: String) {
    VersionCatalogHelper(this).dependency(libName, "compileOnly")
}

fun Project.testRuntimeOnlyFromCatalog(libName: String) {
    VersionCatalogHelper(this).dependency(libName, "testRuntimeOnly")
}

fun Project.platformFromCatalog(libName: String) =
    VersionCatalogHelper(this).platform(libName)

fun Project.idea(block: IdeaModel.() -> Unit) =
    (this as ExtensionAware).extensions.configure("idea", block)