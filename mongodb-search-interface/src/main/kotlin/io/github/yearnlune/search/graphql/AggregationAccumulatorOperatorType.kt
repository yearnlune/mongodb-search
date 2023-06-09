package io.github.yearnlune.search.graphql

enum class AggregationAccumulatorOperatorType {
    COUNT,
    SUM,
    AVERAGE,
    MAX,
    MIN;

    companion object :
        EnumCompanion<AggregationAccumulatorOperatorType, String>(
            AggregationAccumulatorOperatorType.values().associateBy(AggregationAccumulatorOperatorType::name)
        )
}