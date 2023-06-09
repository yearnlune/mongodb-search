package io.github.yearnlune.search.graphql

class DreamChart(
    val title: String,
    val description: String?,
    val type: ChartType,
    val colorPalette: ColorPaletteType,
    val valueType: ChartValueType,
    val xAxis: List<String>,
    val datasets: List<DreamChartDataset>
)