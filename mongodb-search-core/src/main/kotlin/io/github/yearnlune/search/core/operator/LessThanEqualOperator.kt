package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.query.Criteria

class LessThanEqualOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.lte(values[0])

    override fun buildExpression(): AggregationExpression =
        ComparisonOperators.Lte.valueOf(searchBy).lessThanEqualToValue(values[0])
}