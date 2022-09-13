package io.github.yearnlune.search.plugin

import io.github.yearnlune.search.plugin.BuildProperties.GRAPHQL_SCHEMA_FILE_NAME
import io.github.yearnlune.search.plugin.BuildProperties.OUTPUT_SCHEMA_FILE
import io.github.yearnlune.search.plugin.BuildProperties.TARGET_RESOURCES_DIRECTORY
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

open class CopyMongodbSearchInterface : CommonTask() {

    @TaskAction
    fun execute() {
        val targetResourceDirectory = getAbsolutePath(TARGET_RESOURCES_DIRECTORY)
        val mongodbSearchSchema = File(getAbsolutePath(OUTPUT_SCHEMA_FILE))

        if (mongodbSearchSchema.isFile) {
            val targetGraphqlSchema = File(Paths.get(targetResourceDirectory, GRAPHQL_SCHEMA_FILE_NAME).toUri())
            mongodbSearchSchema.copyTo(targetGraphqlSchema, true)
        }
    }
}