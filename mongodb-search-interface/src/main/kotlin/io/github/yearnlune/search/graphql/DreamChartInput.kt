package io.github.yearnlune.search.graphql

class DreamChartInput(
    val title: String,
    val description: String?,
    val type: ChartType,
    val stacked: Boolean,
    val colorPalette: ColorPaletteType,
    val valueType: ChartValueType,
    val datasetMeta: DreamChartDatasetMetaInput
)