package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.graphql.DataInput
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.aggregation.Aggregation

class ConditionOperatorTest : DescribeSpec({
    val search =
        SearchInput.builder()
            .withBy("score")
            .withType(PropertyType.INTEGER)
            .withOperator(SearchOperatorType.BETWEEN)
            .withValue(listOf("0", "60"))
            .build()

    describe("buildExpression") {
        context("조건을 통해 값을 분기할 때") {
            it("\$cond query를 반환한다.") {
                val conditionExpression = ConditionOperator(
                    search,
                    DataInput.builder().withType(PropertyType.STRING).withValue("F").build(),
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