package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class MinOperator(
    private val propertyPath: String?,
    private val propertyExpression: AggregationExpression? = null,
    alias: String? = null
) : AggregateOperator() {

    override val finalAlias: String by lazy { alias ?: "${propertyPath}_min" }

    override fun validate(): Boolean = !propertyPath.isNullOrBlank() || propertyExpression != null

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return when (aggregationOperation) {
            is GroupOperation -> {
                if (propertyPath != null) {
                    aggregationOperation.min(propertyPath).`as`(finalAlias)
                } else {
                    aggregationOperation.min(propertyExpression!!).`as`(finalAlias)
                }
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun buildExpression(): AggregationExpression = AccumulatorOperators.Min.minOf(propertyPath!!)
}