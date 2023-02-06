package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.operator.AggregateOperatorDelegator
import io.github.yearnlune.search.core.operator.SearchOperatorDelegator
import io.github.yearnlune.search.graphql.DateUnitType
import io.github.yearnlune.search.graphql.SearchInput
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

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
    val operations = mutableListOf<AggregationOperation>()
    operations.addAll(this.pipeline.operations)

    aggregates.forEach {
        operations.addAll(
            AggregateOperatorDelegator()
                .create(
                    if (it is Map<*, *>) it.toAggregationInput() else it,
                    targetClass
                )
                .buildAggregate()
        )
    }

    return Aggregation.newAggregation(operations)
}

fun List<String>.toMongoFields(): Fields {
    var fields = Fields.fields()

    this.forEach {
        fields = fields.and(it.replace(".", "@"), "\$$it")
    }

    return fields
}

fun DateUnitType.toTemporalType(): TemporalUnit {
    return when (this) {
        DateUnitType.DAYS -> ChronoUnit.DAYS
        DateUnitType.MONTHS -> ChronoUnit.MONTHS
        DateUnitType.WEEKS -> ChronoUnit.WEEKS
        DateUnitType.YEARS -> ChronoUnit.YEARS
    }
}