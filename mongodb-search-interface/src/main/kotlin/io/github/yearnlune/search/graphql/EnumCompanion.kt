package io.github.yearnlune.search.graphql

abstract class EnumCompanion<E, V>(
    private val map: Map<V, E>
) {
    fun fromValue(value: V): E = map[value] ?: throw RuntimeException("NOT FOUND")
}