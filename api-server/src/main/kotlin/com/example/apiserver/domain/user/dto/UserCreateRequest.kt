package com.example.apiserver.domain.user.dto

import com.example.apiserver.domain.user.entity.User
import org.mindrot.jbcrypt.BCrypt

data class UserCreateRequest(
    val username: String,
    val password: String,
    val name: String
) {

    fun toEntity(): User {
        val encodePassword = BCrypt.hashpw(password, BCrypt.gensalt())

        return User(
            username = username,
            password = encodePassword,
            name = name
        )
    }

}

