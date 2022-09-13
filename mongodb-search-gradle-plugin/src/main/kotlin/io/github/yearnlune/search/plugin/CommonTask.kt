package io.github.yearnlune.search.plugin

import org.gradle.api.DefaultTask
import java.nio.file.Paths

open class CommonTask : DefaultTask() {

    protected fun getAbsolutePath(path: String): String = Paths.get(getRootDir(), path).toString()

    private fun getRootDir(): String = super.getProject().rootDir.path
}