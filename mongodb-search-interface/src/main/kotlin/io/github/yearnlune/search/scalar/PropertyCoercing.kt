package io.github.yearnlune.search.scalar

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingSerializeException

class PropertyCoercing : Coercing<String, String> {

    override fun serialize(dataFetcherResult: Any): String {
        return when (dataFetcherResult) {
            is String -> PropertyFactory.convert(dataFetcherResult, true)
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
            is StringValue -> PropertyFactory.convert(input.value)
            else -> throw CoercingSerializeException(
                "Expected a 'String' but was ${input.javaClass.simpleName}."
            )
        }
    }
}