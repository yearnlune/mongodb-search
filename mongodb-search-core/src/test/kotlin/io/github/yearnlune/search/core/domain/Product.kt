package io.github.yearnlune.search.core.domain

data class Product(
    val id: String,
    val name: String,
    val category: Long,
    val price: Double,
    val stockQuantity: Long,
    val updatedAt: Long,
    val deleted: Boolean? = null
)