package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import io.github.yearnlune.search.core.exception.NotSupportedOperationException
import io.github.yearnlune.search.core.exception.ValidationException
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.AggregationOperation

abstract class AggregateOperator : Operator {

    open val finalAlias: String = "alias"

    open var operation: AggregationOperation? = null

    override fun validate(): Boolean = true

    fun buildQuery(aggregationOperation: AggregationOperation? = null): AggregationOperation {
        return if (validate()) {
            buildOperation(aggregationOperation)
        } else {
            throw ValidationException()
        }
    }

    protected open fun buildOperation(aggregationOperation: AggregationOperation? = null): AggregationOperation =
        throw NotSupportedOperationException("Not supported operation: ${this::class.java.simpleName}")

    open fun buildExpression(): AggregationExpression =
        throw NotSupportedExpressionException("Not supported expression: ${this::class.java.simpleName}")
}