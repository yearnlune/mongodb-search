package io.github.yearnlune.search.graphql

enum class ChartType {
    HORIZONTAL_BAR,
    VERTICAL_BAR,
    COUNT,
    PIE,
    DOUGHNUT,
    LINE,
    TABLE;

    companion object : EnumCompanion<ChartType, String>(
        ChartType.values().associateBy(ChartType::name)
    )
}