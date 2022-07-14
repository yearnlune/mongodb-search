package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class CountOperator : AggregateOperator() {

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return when (aggregationOperation) {
            is GroupOperation -> {
                aggregationOperation.count().`as`("count")
            }
            else -> {
                Aggregation.count().`as`("count")
            }
        }
    }
}