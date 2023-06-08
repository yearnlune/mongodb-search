package io.github.yearnlune.search.example.domain.entity.repository.custom.impl

import io.github.yearnlune.search.core.extension.search
import io.github.yearnlune.search.example.domain.entity.User
import io.github.yearnlune.search.example.domain.entity.repository.custom.UserCustomRepository
import io.github.yearnlune.search.example.graphql.UserPageResponse
import io.github.yearnlune.search.graphql.PageInput
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.query.Criteria

class UserCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : UserCustomRepository {

    override fun findUsers(page: PageInput): UserPageResponse {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria("deleted").`is`(false).search(page.searches, User::class.java)
            ),
            Aggregation.facet(
                Aggregation.skip((page.pageNumber - 1) * page.pageSize),
                Aggregation.limit(page.pageSize)
            ).`as`("users")
                .and(Aggregation.count().`as`("count")).`as`("total"),
            Aggregation.addFields().addField("total").withValueOf(ArrayOperators.arrayOf("total.count").elementAt(0))
                .build()
        )

        return mongoTemplate.aggregate(aggregation, "users", UserPageResponse::class.java).mappedResults[0]
    }
}