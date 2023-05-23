package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class MaxOperator(
    private val propertyPath: String?,
    private val propertyExpression: AggregationExpression? = null,
    alias: String? = null
) : AggregateOperator() {

    override val finalAlias: String by lazy { alias ?: "${propertyPath}_max" }

    override fun validate(): Boolean = !propertyPath.isNullOrBlank() || propertyExpression != null

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return when (aggregationOperation) {
            is GroupOperation -> {
                if (propertyPath != null) {
                    aggregationOperation.max(propertyPath).`as`(finalAlias)
                } else {
                    aggregationOperation.max(propertyExpression!!).`as`(finalAlias)
                }
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun buildExpression(): AggregationExpression = AccumulatorOperators.Max.maxOf(propertyPath!!)
}