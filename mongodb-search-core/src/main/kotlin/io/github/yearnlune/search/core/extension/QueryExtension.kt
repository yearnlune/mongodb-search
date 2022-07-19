package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.operator.AggregateOperatorDelegator
import io.github.yearnlune.search.core.operator.SearchOperatorDelegator
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.StatisticInput
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria

fun Criteria.search(searches: List<SearchInput>, targetClass: Class<*>): Criteria {
    var newCriteria = this

    searches.forEach {
        newCriteria = SearchOperatorDelegator().create(it, targetClass).buildQuery(newCriteria)
    }

    return newCriteria
}

fun Aggregation.search(searches: List<SearchInput>, targetClass: Class<*>): Aggregation {
    this.pipeline.add(MatchOperation(Criteria().search(searches, targetClass)))
    return this
}

fun Aggregation.aggregate(aggregates: List<Any>, targetClass: Class<*>): Aggregation {
    aggregates.forEach {
        this.pipeline.add(AggregateOperatorDelegator().create(it, targetClass).buildAggregate())
    }

    return this
}

fun Aggregation.statistic(statistics: StatisticInput, targetClass: Class<*>): Aggregation {
    this.search(statistics.searches, targetClass)
    this.aggregate(statistics.aggregates, targetClass)

    return this
}

fun List<String>.toMongoFields(): Fields {
    var fields = Fields.fields()

    this.forEach {
        fields = fields.and(it, "\$$it")
    }

    return fields
}