package io.github.yearnlune.search.example.controller

import io.github.yearnlune.search.example.graphql.UserDTO
import io.github.yearnlune.search.example.graphql.UserPageResponse
import io.github.yearnlune.search.example.service.UserService
import io.github.yearnlune.search.graphql.PageInput
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserController(
    val userService: UserService
) {

    @QueryMapping
    fun findUser(@Argument id: String): UserDTO {
        return userService.findUser(id)
    }

    @QueryMapping
    fun findUsers(@Argument page: PageInput): UserPageResponse {
        return userService.findUsers(page)
    }
}