package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation

class UnwindOperator(
    private val by: String
) : AggregateOperator() {

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation =
        Aggregation.unwind(by)
}