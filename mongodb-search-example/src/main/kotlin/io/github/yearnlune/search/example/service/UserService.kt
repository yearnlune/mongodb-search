package io.github.yearnlune.search.example.service

import io.github.yearnlune.search.example.domain.entity.repository.UserRepository
import io.github.yearnlune.search.example.graphql.UserDTO
import io.github.yearnlune.search.example.graphql.UserPageResponse
import io.github.yearnlune.search.graphql.PageInput
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) {

    fun findUser(userId: String): UserDTO {
        return userRepository.findById(userId).orElseThrow { RuntimeException() }.toUserDTO()
    }

    fun findUsers(page: PageInput): UserPageResponse {
        return userRepository.findUsers(page)
    }
}