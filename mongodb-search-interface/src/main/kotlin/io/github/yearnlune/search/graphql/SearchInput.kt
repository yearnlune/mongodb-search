package io.github.yearnlune.search.graphql

class SearchInput(
    var by: String,
    var type: PropertyType,
    var operator: SearchOperatorType,
    var value: List<String>,
)