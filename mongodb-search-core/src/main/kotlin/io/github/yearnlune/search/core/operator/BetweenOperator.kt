package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.BooleanOperators
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.query.Criteria

class BetweenOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.gte(values[0]).lt(values[1])

    override fun buildExpression(): AggregationExpression {
        return BooleanOperators.And.and(ComparisonOperators.Gte.valueOf(searchBy).greaterThanEqualToValue(values[0]))
            .andExpression(ComparisonOperators.Lt.valueOf(searchBy).lessThanValue(values[1]))
    }
}