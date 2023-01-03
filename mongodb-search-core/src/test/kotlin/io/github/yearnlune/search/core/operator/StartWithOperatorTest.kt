package io.github.yearnlune.search.core.operator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class StartWithOperatorTest : DescribeSpec({
    val searchBy = "ip"

    describe("buildQuery") {
        context("IP 대역으로 검색하려고 할 때") {
            context("IP 대역이 하나일 때") {
                it("\$regex query를 반환한다.") {
                    val values = listOf("192.168.1")
                    val startWithQuery = StartWithOperator(searchBy, values).buildQuery()
                    val expectedQuery =
                        "{ \"$searchBy\" : { \"\$regularExpression\" : { \"pattern\" : \"^192\\\\.168\\\\.1\", \"options\" : \"iu\"}}}"

                    SerializationUtils.serializeToJsonSafely(Query(startWithQuery).queryObject) shouldBe expectedQuery
                }
            }
            context("IP 대역이 여러개 일 때") {
                it("\$regex query를 반환한다.") {
                    val values = listOf("192.168.1", "192.168.2", "172.224.10")

                    val equalOperator = StartWithOperator(searchBy, values).buildQuery()
                    val expectedQuery =
                        "{ \"$searchBy\" : { \"\$regularExpression\" : { \"pattern\" : \"^192\\\\.168\\\\.1|^192\\\\.168\\\\.2|^172\\\\.224\\\\.10\", \"options\" : \"iu\"}}}"

                    SerializationUtils.serializeToJsonSafely(Query(equalOperator).queryObject) shouldBe expectedQuery
                }
            }
        }
    }

    describe("buildExpression") {
        context("특정 IP로 시작하는 IP를 검색하려고 할 때") {
            it("\$regexMatch 활용하여 query를 반환한다.") {
                val values = listOf("192.168.1")
                val startWithExpression = StartWithOperator(searchBy, values).buildExpression()
                val expectedQuery =
                    "{ \"\$regexMatch\" : { \"input\" : \"\$$searchBy\", \"regex\" : \"^192\\\\.168\\\\.1\", \"options\" : \"iu\"}}"

                startWithExpression.buildTestAggregation().toString() shouldContain expectedQuery
            }
        }
    }
})