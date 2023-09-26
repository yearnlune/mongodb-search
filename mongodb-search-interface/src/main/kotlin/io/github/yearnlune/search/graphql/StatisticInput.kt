package io.github.yearnlune.search.graphql

class StatisticInput(
    var searches: List<SearchInput> = listOf(),
    var aggregates: List<Any> = listOf()
)