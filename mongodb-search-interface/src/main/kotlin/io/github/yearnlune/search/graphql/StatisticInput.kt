package io.github.yearnlune.search.graphql

class StatisticInput(
    val searches: List<SearchInput> = listOf(),
    val aggregates: List<Any> = listOf()
)