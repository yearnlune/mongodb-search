package io.github.yearnlune.search.graphql

class DreamChartInput(
    val title: String,
    val description: String? = null,
    val type: ChartType,
    val stacked: Boolean = false,
    val colorPalette: ColorPaletteType,
    val valueType: ChartValueType,
    val datasetMeta: DreamChartDatasetMetaInput
)