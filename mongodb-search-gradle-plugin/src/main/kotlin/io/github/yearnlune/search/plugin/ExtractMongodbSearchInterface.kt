package io.github.yearnlune.search.plugin

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

open class ExtractMongodbSearchInterface : CommonTask() {

    @TaskAction
    fun execute() {
        getImplementationFileTree()
            .filter { it.name.startsWith(BuildProperties.INTERFACE_NAME) }
            .map {
                val outputDir = getAbsoluteBuildPath(BuildProperties.OUTPUT_DIRECTORY)
                unzipTo(File(outputDir), it)
            }
    }

    private fun getImplementationFileTree(): FileTree {
        return super.getProject().configurations.getByName("implementation").asFileTree
    }

    private fun unzipTo(outputDirectory: File, zipFile: File) {
        return ZipFile(zipFile).use { zip ->
            for (entry in zip.entries()) {
                unzipEntryTo(outputDirectory, zip, entry)
            }
        }
    }

    private fun unzipEntryTo(outputDirectory: File, zip: ZipFile, entry: ZipEntry) {
        val output = outputDirectory.resolve(entry.name)
        if (entry.isDirectory) {
            output.mkdirs()
        } else {
            output.parentFile.mkdirs()
            zip.getInputStream(entry).use {
                it.copyTo(output)
            }
        }
    }

    private fun InputStream.copyTo(file: File): Long = file.outputStream().use { copyTo(it) }
}