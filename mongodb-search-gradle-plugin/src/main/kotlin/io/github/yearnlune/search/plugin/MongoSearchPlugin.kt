package io.github.yearnlune.search.plugin

import io.github.yearnlune.search.plugin.BuildProperties.APPLY_MONGODB_SEARCH_TASK
import io.github.yearnlune.search.plugin.BuildProperties.COPY_INTERFACE_TASK
import io.github.yearnlune.search.plugin.BuildProperties.EXTRACT_INTERFACE_TASK
import io.github.yearnlune.search.plugin.BuildProperties.OUTPUT_RESOURCE_DIRECTORY
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.nio.file.Paths

class MongoSearchPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        registerTasks(project)

        project.afterEvaluate {
            initializePlugin(project)
        }
    }

    private fun initializePlugin(project: Project) {
        addResourceToSourcesSet(project)
    }

    private fun registerTasks(project: Project) {
        registerExtractInterface(project)
        registerCopyInterface(project)
        registerApplyMongodbSearch(project)
    }

    private fun registerExtractInterface(project: Project) {
        project.tasks.register(EXTRACT_INTERFACE_TASK, ExtractMongodbSearchInterface::class.java)
        project.getTasksByName(EXTRACT_INTERFACE_TASK, false)

        project.plugins.apply(JavaPlugin::class.java)
    }

    private fun registerCopyInterface(project: Project) {
        project.tasks.register(COPY_INTERFACE_TASK, CopyMongodbSearchInterface::class.java)
        project.getTasksByName(COPY_INTERFACE_TASK, false)
            .forEach { copyTask ->
                copyTask.dependsOn(EXTRACT_INTERFACE_TASK)
            }

        project.plugins.apply(JavaPlugin::class.java)
    }

    private fun registerApplyMongodbSearch(project: Project) {
        project.tasks.register(APPLY_MONGODB_SEARCH_TASK, ApplyMongodbSearch::class.java)
        project.getTasksByName(APPLY_MONGODB_SEARCH_TASK, false)
            .forEach {
                it.dependsOn(COPY_INTERFACE_TASK)
                project.getTasksByName("compileJava", false)
                    .map { task -> task.dependsOn(it.path) }
            }

        project.plugins.apply(JavaPlugin::class.java)
    }

    private fun addResourceToSourcesSet(project: Project) {
        (project.extensions.getByName("sourceSets") as SourceSetContainer)
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            .resources {
                val resourceDir = File(Paths.get(project.buildDir.path, OUTPUT_RESOURCE_DIRECTORY).toUri())
                it.srcDirs(resourceDir)
            }
    }
}