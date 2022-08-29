package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.MongoSearch
import io.github.yearnlune.search.graphql.DreamChart
import io.github.yearnlune.search.graphql.DreamChartDataset
import io.github.yearnlune.search.graphql.DreamChartInput
import org.springframework.data.mongodb.core.MongoTemplate

fun MongoTemplate.dreamChart(chart: DreamChartInput): DreamChart {
    val results = this.aggregate(
        MongoSearch.statistic(chart.datasetMeta.statistic, Any::class.java),
        chart.datasetMeta.collection,
        Map::class.java
    ).mappedResults

    val keys = results
        .map {
            when (val keyCandidate = it["_id"]) {
                null -> "null"
                is String -> keyCandidate
                is Map<*, *> -> {
                    keyCandidate.keys.toList()
                        .sortedBy { k -> k as String }
                        .map { key -> keyCandidate[key] }
                        .joinToString("@")
                }
                else -> throw IllegalArgumentException()
            }
        }
    val values: MutableMap<String, MutableList<Long>> = mutableMapOf()
    results.forEach { row ->
        row.filter { it.key != "_id" }
            .map {
                values.compute(it.key as String) { _, v ->
                    val list = v ?: mutableListOf()
                    list.add(it.value.toString().toLong())
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