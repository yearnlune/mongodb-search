package io.github.yearnlune.search.graphql

class Statistic(
    val searches: List<Search>,
    val aggregates: List<Map<String, Any>>
)