package io.github.yearnlune.search.plugin

import io.github.yearnlune.search.plugin.BuildProperties.EXTRACTED_GRAPHQL_DIRECTORY
import io.github.yearnlune.search.plugin.BuildProperties.OUTPUT_GRAPHQL_RESOURCE_DIRECTORY
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CopyMongodbSearchInterface : CommonTask() {

    @TaskAction
    fun execute() {
        val extractedGraphqlDirectory = File(getAbsoluteBuildPath(EXTRACTED_GRAPHQL_DIRECTORY))

        runCatching {
            val outputResourcesDirectory = makeOutputResourcesDirectory()
            if (extractedGraphqlDirectory.isDirectory) {
                extractedGraphqlDirectory.copyRecursively(outputResourcesDirectory, true)
            } else {
                throw RuntimeException("NOT FOUND EXTRACTED GRAPHQL DIRECTORY ${extractedGraphqlDirectory.path}")
            }
        }.onSuccess {
            removeDirectory(extractedGraphqlDirectory)
        }.onFailure {
            it.stackTrace
        }
    }

    private fun makeOutputResourcesDirectory(): File =
        File(getAbsoluteBuildPath(OUTPUT_GRAPHQL_RESOURCE_DIRECTORY)).also { it.mkdirs() }

    private fun removeDirectory(directory: File) = directory.deleteRecursively()
}