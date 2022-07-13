package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.query.Criteria

class EqualOperator(
    override val searchBy: String,
    override val values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun buildQuery(): Criteria = Criteria.where(searchBy).`in`(values)
}
