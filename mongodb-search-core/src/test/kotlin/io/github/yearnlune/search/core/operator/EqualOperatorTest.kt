package io.github.yearnlune.search.core.operator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class EqualOperatorTest : DescribeSpec({

    describe("buildQuery") {
        val searchBy = "name"
        val values = listOf("item#1")

        context("이름을 검색하려고 할 때") {
            it("이름에 대한 \$in query를 반환한다.") {
                val equalOperator = EqualOperator(searchBy, values).buildQuery()
                val expectedQuery =
                    "{ \"$searchBy\" : { \"\$in\" : ${jacksonObjectMapper().writeValueAsString(values)}}}"

                SerializationUtils.serializeToJsonSafely(Query(equalOperator).queryObject) shouldBe expectedQuery
            }
        }
    }
})