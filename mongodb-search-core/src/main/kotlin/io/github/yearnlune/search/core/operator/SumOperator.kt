package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class SumOperator(
    private val property: String
) : AggregateOperator() {

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return when (aggregationOperation) {
            is GroupOperation -> {
                if (property.isBlank()) {
                    throw java.lang.IllegalArgumentException()
                }

                aggregationOperation.sum(property).`as`("${property}_sum")
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

}