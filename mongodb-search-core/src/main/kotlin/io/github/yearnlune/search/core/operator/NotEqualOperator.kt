package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.query.Criteria

class NotEqualOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.nin(values)

    override fun buildExpression(): AggregationExpression {
        return ComparisonOperators.Ne.valueOf(searchBy).notEqualToValue(values[0])
    }
}