package io.github.yearnlune.search.core.operator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class EqualOperatorTest : DescribeSpec({
    val searchBy = "name"
    val values = listOf("item#1")

    describe("buildQuery") {
        context("이름을 검색하려고 할 때") {
            it("이름에 대한 \$in query를 반환한다.") {
                val equalQuery = EqualOperator(searchBy, values).buildQuery()
                val expectedQuery =
                    "{ \"$searchBy\" : { \"\$in\" : ${jacksonObjectMapper().writeValueAsString(values)}}}"

                SerializationUtils.serializeToJsonSafely(Query(equalQuery).queryObject) shouldBe expectedQuery
            }
        }
    }

    describe("buildExpression") {
        context("이름을 검색하려고 할 때") {
            it("이름에 대한 \$eq query를 반환한다.") {
                val equalExpression = EqualOperator(searchBy, values).buildExpression()
                val expectedQuery = "{ \"\$eq\" : [\"\$name\", \"item#1\"]}"

                equalExpression.buildTestAggregation().toString() shouldContain expectedQuery
            }
        }
    }
})