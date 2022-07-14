package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.operator.AggregateOperatorDelegator
import io.github.yearnlune.search.core.operator.SearchOperatorDelegator
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.StatisticInput
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.query.Criteria

fun <T> Criteria.search(searches: List<SearchInput>, targetClass: Class<T>): Criteria {
    searches.forEach {
        this.andOperator(SearchOperatorDelegator.create(it, targetClass).buildQuery())
    }

    return this
}

fun <T> Aggregation.search(searches: List<SearchInput>, targetClass: Class<T>): Aggregation {
    searches.forEach {
        this.pipeline.add(SearchOperatorDelegator.create(it, targetClass).buildMatchOperation())
    }

    return this
}

fun <T> Aggregation.statistic(statistics: List<StatisticInput>, targetClass: Class<T>): Aggregation {
    statistics.forEach {
        this.pipeline.add(
            AggregateOperatorDelegator
                .create(it, targetClass)
                .buildAggregate()
        )
    }

    return this
}

fun List<String>.toMongoFields(): Fields {
    var fields = Fields.fields()

    this.forEach {
        fields = fields.and(it, "\$$it")
    }

    return fields
}