package io.github.yearnlune.search.scalar

import graphql.schema.Coercing
import graphql.schema.CoercingSerializeException

class PropertyCoercing : Coercing<String, String> {

    private val propertyFactory = PropertyFactory

    override fun serialize(dataFetcherResult: Any): String {
        return when (dataFetcherResult) {
            is String -> PropertyFactory.convert(dataFetcherResult)
            else -> throw CoercingSerializeException(
                "Expected a 'String' but was ${dataFetcherResult.javaClass.simpleName}."
            )
        }
    }

    override fun parseValue(input: Any): String {
        return when (input) {
            is String -> PropertyFactory.convert(input)
            else -> throw CoercingSerializeException(
                "Expected a 'String' but was ${input.javaClass.simpleName}."
            )
        }
    }

    override fun parseLiteral(input: Any): String {
        return when (input) {
            is String -> PropertyFactory.convert(input)
            else -> throw CoercingSerializeException(
                "Expected a 'String' but was ${input.javaClass.simpleName}."
            )
        }
    }
}