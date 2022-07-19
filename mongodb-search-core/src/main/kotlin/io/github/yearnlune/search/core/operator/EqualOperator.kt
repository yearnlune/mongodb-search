package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.query.Criteria

class EqualOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.`in`(values)
}
