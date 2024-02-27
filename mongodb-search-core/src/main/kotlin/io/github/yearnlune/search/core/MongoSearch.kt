package io.github.yearnlune.search.core

import io.github.yearnlune.search.core.extension.aggregate
import io.github.yearnlune.search.core.extension.search
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.StatisticInput
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria

object MongoSearch {

    fun search(searches: List<SearchInput>, targetClass: Class<*>? = null): Criteria {
        return Criteria().search(searches, targetClass)
    }

    fun statistic(statisticInput: StatisticInput, targetClass: Class<*>? = null): Aggregation {
        return Aggregation.newAggregation(
            Aggregation.match(Criteria().search(statisticInput.searches, targetClass))
        ).aggregate(statisticInput.aggregates, targetClass)
    }
}