package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation

class LimitOperator(private val maxElements: Long) : AggregateOperator() {

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return Aggregation.limit(maxElements)
    }
}