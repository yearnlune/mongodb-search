package io.github.yearnlune.search.core.type

import io.github.yearnlune.search.core.exception.NotFoundFieldException

abstract class EnumCompanion<E, V>(
    private val map: Map<V, E>
) {
    fun fromValue(value: V): E = map[value] ?: throw NotFoundFieldException("")
}