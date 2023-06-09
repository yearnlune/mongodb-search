package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.graphql.DataInput
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.aggregation.Aggregation

class ConditionOperatorTest : DescribeSpec({
    val search = SearchInput(
        by = "score",
        type = PropertyType.INTEGER,
        operator = SearchOperatorType.BETWEEN,
        value = listOf("0", "60")
    )

    describe("buildExpression") {
        context("조건을 통해 값을 분기할 때") {
            it("\$cond query를 반환한다.") {
                val conditionExpression = ConditionOperator(
                    search,
                    DataInput(type = PropertyType.STRING, value = "F"),
                    "P"
                ).buildExpression()
                val expectedQuery =
                    "{ \"\$cond\" : " +
                        "{ \"if\" : { \"\$and\" : [" +
                        "{ \"\$gte\" : [\"\$${search.by}\", ${search.value[0]}]}, " +
                        "{ \"\$lt\" : [\"\$${search.by}\", ${search.value[1]}]}" +
                        "]}, \"then\" : \"F\", \"else\" : \"P\"}}"

                Aggregation.newAggregation(Aggregation.project().and(conditionExpression).`as`("PASS-FAIL"))
                    .toString() shouldContain expectedQuery
            }
        }
    }
})