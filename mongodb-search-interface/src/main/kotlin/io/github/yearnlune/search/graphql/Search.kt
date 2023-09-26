package io.github.yearnlune.search.graphql

class Search(
    val by: String,
    val type: PropertyType,
    val operator: SearchOperatorType,
    val value: List<String>
)