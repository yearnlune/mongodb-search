package io.github.yearnlune.search.example.domain.entity

import io.github.yearnlune.search.example.graphql.UserDTO
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import org.bson.types.ObjectId

@Document("users")
class User(
    @Id
    val id: String = ObjectId().toString(),

    var name: String,

    var bio: String?,

    var location: String?,

    var email: String,

    var homepage: String?,

    var updatedAt: LocalDateTime,

    val createdAt: LocalDateTime,

    var deleted: Boolean = false
) {

    fun toUserDTO(): UserDTO {
        return UserDTO.builder()
            .withId(this.id)
            .withName(this.name)
            .withEmail(this.email)
            .withHomepage(this.homepage)
            .withLocation(this.location)
            .withUpdatedAt(this.updatedAt)
            .build()
    }
}