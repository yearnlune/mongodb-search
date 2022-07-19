package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class CountOperator(alias: String? = null) : AggregateOperator() {

    override val finalAlias: String by lazy { alias ?: "count" }

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return when (aggregationOperation) {
            is GroupOperation -> {
                aggregationOperation.count().`as`(finalAlias)
            }
            else -> {
                Aggregation.count().`as`(finalAlias)
            }
        }
    }
}