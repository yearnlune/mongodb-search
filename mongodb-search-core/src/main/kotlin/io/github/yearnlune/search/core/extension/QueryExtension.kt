package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.operator.SearchOperatorDelegator
import io.github.yearnlune.search.graphql.SearchInput
import org.springframework.data.mongodb.core.query.Criteria

fun <T> Criteria.search(searches: List<SearchInput>, targetClass: Class<T>): Criteria {
    searches.forEach {
        this.andOperator(
            SearchOperatorDelegator
                .create(it, targetClass)
                .buildQuery()
        )
    }

    return this
}
