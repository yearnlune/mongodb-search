package io.github.yearnlune.search.plugin

import org.gradle.api.DefaultTask
import java.nio.file.Paths

open class CommonTask : DefaultTask() {

    protected fun getAbsoluteBuildPath(path: String): String = Paths.get(getBuildDir(), path).toString()

    private fun getBuildDir(): String = super.getProject().buildDir.path
}