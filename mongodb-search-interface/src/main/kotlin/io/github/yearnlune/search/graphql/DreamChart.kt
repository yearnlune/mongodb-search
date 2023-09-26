package io.github.yearnlune.search.graphql

class DreamChart(
    val title: String,
    val description: String? = null,
    val type: ChartType,
    val stacked: Boolean = false,
    val colorPalette: ColorPaletteType,
    val valueType: ChartValueType,
    var xAxis: List<String>,
    val datasets: List<DreamChartDataset>
)