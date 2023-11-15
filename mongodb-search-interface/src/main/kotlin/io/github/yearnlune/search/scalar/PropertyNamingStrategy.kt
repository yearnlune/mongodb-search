package io.github.yearnlune.search.scalar

sealed interface PropertyNamingStrategy {
    fun process(string: String): String
}

object PropertyNamingStrategyOrigin : PropertyNamingStrategy {
    override fun process(string: String): String = string
}

object PropertyNamingStrategyCamel : PropertyNamingStrategy {
    override fun process(string: String): String {
        return "(_[a-z])".toRegex().replace(string.lowercase()) {
            it.value.replace("_", "").uppercase()
        }
    }
}

object PropertyNamingStrategySnake : PropertyNamingStrategy {
    override fun process(string: String): String {
        return "(?<=[a-zA-Z])[A-Z]".toRegex().replace(string) {
            "_${it.value}"
        }.lowercase()
    }
}