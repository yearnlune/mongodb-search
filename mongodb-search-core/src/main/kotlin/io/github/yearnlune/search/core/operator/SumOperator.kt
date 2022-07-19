package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class SumOperator(
    private val propertyPath: String,
    alias: String? = null
) : AggregateOperator() {

    override val finalAlias: String by lazy { alias ?: "${propertyPath}_sum" }

    override fun validate(): Boolean {
        return propertyPath.isNotBlank()
    }

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return when (aggregationOperation) {
            is GroupOperation -> aggregationOperation.sum(propertyPath).`as`(finalAlias)
            else -> throw IllegalArgumentException()
        }
    }
}