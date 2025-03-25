import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.ProjectSettings

plugins {
    alias(libs.plugins.ideaExt)
}

fun IdeaProject.settings(block: ProjectSettings.() -> Unit) =
    (this@settings as ExtensionAware).extensions.configure(block)
