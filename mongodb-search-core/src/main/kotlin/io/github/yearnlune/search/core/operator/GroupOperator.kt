package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.extension.toMongoFields
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation

class GroupOperator(
    private val groupByList: List<String>
) : AggregateOperator() {

    override var operation: AggregationOperation? = Aggregation.group(groupByList.toMongoFields())

    override fun validate(): Boolean = groupByList.isNotEmpty()

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        return this.operation!!
    }
}