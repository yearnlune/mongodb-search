package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators

fun AggregationExpression.buildTestAggregation(): Aggregation {
    return Aggregation.newAggregation(
        Aggregation.project()
            .and(ConditionalOperators.`when`(this).then(true).otherwise(false))
            .`as`("test")
    )
}