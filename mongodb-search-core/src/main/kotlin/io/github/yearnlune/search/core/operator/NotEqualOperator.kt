package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import io.github.yearnlune.search.graphql.ComparisonOperatorType
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.query.Criteria

class NotEqualOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.nin(values)

    override fun buildExpression(operatorType: Any?): AggregationExpression {
        return when (operatorType) {
            ComparisonOperatorType.NOT_EQUAL -> ComparisonOperators.Ne.valueOf(searchBy).notEqualToValue(values[0])
            else -> throw NotSupportedExpressionException("")
        }
    }
}