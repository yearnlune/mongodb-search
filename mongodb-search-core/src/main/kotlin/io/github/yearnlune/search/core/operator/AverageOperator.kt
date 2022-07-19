package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class AverageOperator(
    private val propertyPath: String,
    alias: String? = null
) : AggregateOperator() {

    override val finalAlias: String by lazy { alias ?: "${propertyPath}_avg" }

    override fun validate(): Boolean = propertyPath.isNotBlank()

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return when (aggregationOperation) {
            is GroupOperation -> aggregationOperation.avg(propertyPath).`as`(finalAlias)
            else -> throw IllegalArgumentException()
        }
    }

    override fun buildExpression(): AggregationExpression = AccumulatorOperators.Avg.avgOf(propertyPath)
}