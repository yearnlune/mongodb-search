package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.extension.toMongoFields
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.GroupOperation

class GroupOperator(
    groupBy: List<String>
) : AggregateOperator() {

    val groupOperation: GroupOperation = Aggregation.group(groupBy.toMongoFields())
}