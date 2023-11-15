package io.github.yearnlune.search.scalar

object PropertyFactory {
    private val factory: MutableMap<String, String> = mutableMapOf("id" to "_id")
    private val reverseFactory: MutableMap<String, String> = mutableMapOf("_id" to "id")

    fun add(from: String, to: String): MutableMap<String, String> {
        factory[from] = to
        reverseFactory[to] = from
        return factory
    }

    fun addAll(map: Map<String, String>): MutableMap<String, String> {
        factory.putAll(map)
        reverseFactory.putAll(map.map { it.value to it.key })
        return factory
    }

    fun convert(target: String, reverse: Boolean = false): String {
        return if (!reverse) {
            factory[target] ?: target
        } else {
            reverseFactory[target] ?: target
        }
    }
}