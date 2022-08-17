package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.graphql.DreamChart
import io.github.yearnlune.search.graphql.DreamChartDataset
import io.github.yearnlune.search.graphql.DreamChartInput
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation

fun MongoTemplate.dreamChart(chart: DreamChartInput): DreamChart {
    val results = this.aggregate(
        Aggregation.newAggregation()
            .search(chart.datasetMeta.statistic.searches, Any::class.java)
            .aggregate(chart.datasetMeta.statistic.aggregates, Any::class.java),
        chart.datasetMeta.collection,
        Map::class.java
    ).mappedResults

    val keys = results
        .filter { it["_id"] != null }
        .map { it["_id"] as String}
    val values: MutableMap<String, MutableList<Long>> = mutableMapOf()
    results.forEach { row ->
        row.filter { it.key != "_id" }
            .map {
                values.compute(it.key as String) { _, v ->
                    val list = v ?: mutableListOf()
                    list.add(it.value as Long)
                    list
                }
            }
    }

    val datasets = values.map {
        DreamChartDataset.builder()
            .withLabel(it.key)
            .withData(it.value)
            .build()
    }

    return DreamChart.builder()
        .withTitle(chart.title)
        .withType(chart.type)
        .withDescription(chart.description)
        .withColorPalette(chart.colorPalette)
        .withValueType(chart.valueType)
        .withXAxis(keys)
        .withDatasets(datasets)
        .build()
}