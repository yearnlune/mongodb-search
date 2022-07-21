package io.github.yearnlune.search.`interface`

import graphql.schema.GraphQLScalarType

object AggregationScalar {

    val INSTANCE: GraphQLScalarType = GraphQLScalarType.newScalar()
        .name("Aggregation")
        .description("Aggregation Operation")
        .build()
}