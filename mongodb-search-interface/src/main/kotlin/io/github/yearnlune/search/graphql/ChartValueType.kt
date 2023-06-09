package io.github.yearnlune.search.graphql

enum class ChartValueType {
    ORIGIN,
    PERCENTAGE;

    companion object : EnumCompanion<ChartValueType, String>(
        ChartValueType.values().associateBy(ChartValueType::name)
    )
}