package io.github.yearnlune.search.example.domain.entity.repository

import io.github.yearnlune.search.example.domain.entity.User
import io.github.yearnlune.search.example.domain.entity.repository.custom.UserCustomRepository
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String>, UserCustomRepository