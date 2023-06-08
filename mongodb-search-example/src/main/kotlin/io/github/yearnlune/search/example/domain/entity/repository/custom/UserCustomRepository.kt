package io.github.yearnlune.search.example.domain.entity.repository.custom

import io.github.yearnlune.search.example.graphql.UserPageResponse
import io.github.yearnlune.search.graphql.PageInput

interface UserCustomRepository {

    fun findUsers(page: PageInput): UserPageResponse
}