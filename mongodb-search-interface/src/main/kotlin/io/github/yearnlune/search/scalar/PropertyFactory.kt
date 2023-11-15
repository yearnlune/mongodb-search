package io.github.yearnlune.search.scalar

object PropertyFactory {
    private val factory: MutableMap<String, String> = mutableMapOf("id" to "_id")

    fun add(from: String, to: String): MutableMap<String, String> {
        factory[from] = to
        return factory
    }

    fun addAll(map: Map<String, String>): MutableMap<String, String> {
        factory.putAll(map)
        return factory
    }

    fun convert(from: String): String {
        return factory[from] ?: from
    }
}