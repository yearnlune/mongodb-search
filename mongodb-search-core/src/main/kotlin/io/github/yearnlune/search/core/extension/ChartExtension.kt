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
                else -> keyCandidate.toString()
            }
        }
    val values: MutableMap<String, MutableList<Double>> = mutableMapOf()
    results.forEach { row ->
        row.filter { it.key != "_id" }
            .map {
                values.compute(it.key as String) { _, v ->
                    val list = v ?: mutableListOf()
                    list.add(it.value.toString().toDouble())
                    list
                }
            }
    }

    val datasets = values.map {
        DreamChartDataset(label = it.key, data = it.value.toMutableList())
    }

    return DreamChart(
        title = chart.title,
        type = chart.type,
        stacked = chart.stacked,
        description = chart.description,
        colorPalette = chart.colorPalette,
        valueType = chart.valueType,
        xAxis = keys,
        datasets = datasets
    )
}