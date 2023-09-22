package io.github.yearnlune.search.graphql

class GroupAggregationInput(
    val by: List<GroupByInput> = listOf(),
    val aggregations: List<AggregationInput> = listOf()
)