package io.github.yearnlune.search.graphql

class AggregationInput(
    val property: String? = null,
    val propertyExpression: ConditionInput? = null,
    val operator: AggregationAccumulatorOperatorType,
    val alias: String? = null,
)