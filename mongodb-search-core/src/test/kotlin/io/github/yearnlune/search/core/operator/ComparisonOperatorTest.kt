package io.github.yearnlune.search.core.operator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class ComparisonOperatorTest : DescribeSpec({
    val searchBy = "price"
    val values = listOf(10.0)

    describe("LessThanOperator") {
        context("buildQuery") {
            it("특정 값 보다 작은 경우") {
                val lessThanQuery = LessThanOperator(searchBy, values).buildQuery()
                val expectedQuery = "{ \"$searchBy\" : { \"\$lt\" : ${values[0]}}}"

                SerializationUtils.serializeToJsonSafely(Query(lessThanQuery).queryObject) shouldBe expectedQuery
            }
        }
    }

    describe("LessThanEqualOperator") {
        context("buildQuery") {
            it("특정 값 보다 작거나 같은 경우") {
                val lessThanEqualQuery = LessThanEqualOperator(searchBy, values).buildQuery()
                val expectedQuery = "{ \"$searchBy\" : { \"\$lte\" : ${values[0]}}}"

                SerializationUtils.serializeToJsonSafely(Query(lessThanEqualQuery).queryObject) shouldBe expectedQuery
            }
        }
    }

    describe("GreaterThanOperator") {
        context("buildQuery") {
            it("특정 값 보다 큰 경우") {
                val greaterThanQuery = GreaterThanOperator(searchBy, values).buildQuery()
                val expectedQuery = "{ \"$searchBy\" : { \"\$gt\" : ${values[0]}}}"

                SerializationUtils.serializeToJsonSafely(Query(greaterThanQuery).queryObject) shouldBe expectedQuery
            }
        }
    }

    describe("GreaterThanEqualOperator") {
        context("buildQuery") {
            it("특정 값 보다 크거나 같은 경우") {
                val greaterThanEqualQuery = GreaterThanEqualOperator(searchBy, values).buildQuery()
                val expectedQuery = "{ \"$searchBy\" : { \"\$gte\" : ${values[0]}}}"

                SerializationUtils.serializeToJsonSafely(Query(greaterThanEqualQuery).queryObject) shouldBe expectedQuery
            }
        }
    }
})