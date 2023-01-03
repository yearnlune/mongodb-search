package io.github.yearnlune.search.core.operator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class ContainOperatorTest : DescribeSpec({
    val searchBy = "item"

    describe("buildQuery") {
        context("제품을 검색하려고 할 때") {
            context("제품이 하나일 때") {
                it("제품에 대한 \$regex query를 반환한다.") {
                    val values = listOf("apple")
                    val containQuery = ContainOperator(searchBy, values).buildQuery()
                    val expectedQuery =
                        "{ \"$searchBy\" : { \"\$regularExpression\" : { \"pattern\" : \"${values.first()}\", \"options\" : \"iu\"}}}"

                    SerializationUtils.serializeToJsonSafely(Query(containQuery).queryObject) shouldBe expectedQuery
                }
            }
            context("제품이 여러개 일 때") {
                it("제품에 대한 \$regex query를 반환한다.") {
                    val values = listOf("apple", "banana", "pineapple")
                    val containQuery = ContainOperator(searchBy, values).buildQuery()
                    val expectedQuery =
                        "{ \"$searchBy\" : { \"\$regularExpression\" : { \"pattern\" : \"${values.joinToString("|")}\", \"options\" : \"iu\"}}}"

                    SerializationUtils.serializeToJsonSafely(Query(containQuery).queryObject) shouldBe expectedQuery
                }
            }
        }

        context("제품을 특별한 문자가 포함된 검색어로 검색하려고 할 때") {
            it("제품에 대한 \$regex query를 반환한다.") {
                val values = listOf("apple.3++")
                val containQuery = ContainOperator(searchBy, values).buildQuery()
                val expectedQuery =
                    "{ \"$searchBy\" : { \"\$regularExpression\" : { \"pattern\" : \"apple\\\\.3\\\\+\\\\+\", \"options\" : \"iu\"}}}"

                SerializationUtils.serializeToJsonSafely(Query(containQuery).queryObject) shouldBe expectedQuery
            }
        }
    }

    describe("buildExpression") {
        context("제품을 검색하려고 할 때") {
            it("제품에 대한 \$regexMatch query를 반환한다.") {
                val values = listOf("apple")
                val containExpression = ContainOperator(searchBy, values).buildExpression()
                val expectedQuery =
                    "{ \"\$regexMatch\" : { \"input\" : \"\$item\", \"regex\" : \"apple\", \"options\" : \"ui\"}}"

                containExpression.buildTestAggregation().toString() shouldContain expectedQuery
            }

            it("두개 이상의 제품에 대한 \$regexMatch query를 반환한다.") {
                val values = listOf("apple", "banana")
                val containExpression = ContainOperator(searchBy, values).buildExpression()
                val expectedQuery =
                    "{ \"\$regexMatch\" : { \"input\" : \"\$item\", \"regex\" : \"apple|banana\", \"options\" : \"ui\"}}"

                containExpression.buildTestAggregation().toString() shouldContain expectedQuery
            }
        }
    }
})