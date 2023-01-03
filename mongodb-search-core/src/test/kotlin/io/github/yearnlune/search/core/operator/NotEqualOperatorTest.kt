package io.github.yearnlune.search.core.operator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class NotEqualOperatorTest : DescribeSpec({
    val searchBy = "name"
    val values = listOf("item#1")

    describe("buildQuery") {
        context("특정 이름 외 검색하려고 할 때") {
            it("이름에 대한 \$nin query를 반환한다.") {
                val notEqualQuery = NotEqualOperator(searchBy, values).buildQuery()
                val expectedQuery =
                    "{ \"$searchBy\" : { \"\$nin\" : ${jacksonObjectMapper().writeValueAsString(values)}}}"

                SerializationUtils.serializeToJsonSafely(Query(notEqualQuery).queryObject) shouldBe expectedQuery
            }
        }
    }

    describe("buildExpression") {
        context("특정 이름 외 검색하려고 할 때") {
            it("이름에 대한 \$ne query를 반환한다.") {
                val notEqualExpression = NotEqualOperator(searchBy, values).buildExpression()
                val expectedQuery = "{ \"\$ne\" : [\"\$name\", \"item#1\"]}"

                notEqualExpression.buildTestAggregation().toString() shouldContain expectedQuery
            }
        }
    }
})