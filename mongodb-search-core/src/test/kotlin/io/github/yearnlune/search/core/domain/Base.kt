package io.github.yearnlune.search.core.domain

import org.bson.types.ObjectId

abstract class Base(
    val id: String = ObjectId().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean? = null
)