package com.example.apiserver.dummy

import com.example.apiserver.domain.user.entity.User
import org.mindrot.jbcrypt.BCrypt
import org.mindrot.jbcrypt.BCrypt.gensalt

class Dummy {

    companion object {
        fun mockUser(id: Long, username: String, password: String, name: String): User {
            return User(
                id = id,
                username = username,
                password = BCrypt.hashpw(password, gensalt()),
                name = name
            )
        }
    }

}