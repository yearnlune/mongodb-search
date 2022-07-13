package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.query.Criteria

abstract class SearchOperator(
    open val searchBy: String,
    open val values: List<Any>
) : Operator {

    abstract fun buildQuery(): Criteria
}
