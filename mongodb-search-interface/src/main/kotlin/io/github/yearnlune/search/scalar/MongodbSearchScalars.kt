package io.github.yearnlune.search.scalar

import graphql.schema.GraphQLScalarType

object MongodbSearchScalars {

    val Property = GraphQLScalarType.newScalar()
        .name("Property")
        .coercing(PropertyCoercing())
        .build()
}