package io.github.yearnlune.search.graphql

class SearchInput(
    val by: String,
    val type: PropertyType,
    val operator: SearchOperatorType,
    val value: List<String>,
)