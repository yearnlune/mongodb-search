package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationOperation

abstract class AggregateOperator : Operator {

    open fun buildOperation(aggregationOperation: AggregationOperation? = null): AggregationOperation? = null
}
