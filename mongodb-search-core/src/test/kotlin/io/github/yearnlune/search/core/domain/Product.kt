package io.github.yearnlune.search.core.domain

data class Product(
    val name: String,
    val category: Long,
    val price: Double,
    val stockQuantity: Long
) : Base()