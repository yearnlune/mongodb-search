package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.extension.toMongoFields
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation

class GroupOperator(
    private val groupBy: List<String>
) : AggregateOperator() {

    override var operation: AggregationOperation? = Aggregation.group(groupBy.toMongoFields())

    override fun validate(): Boolean = groupBy.isNotEmpty()

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return this.operation!!
    }
}