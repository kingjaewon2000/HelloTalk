package com.example.apiserver.domain.user.repository

import com.example.apiserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun existsByUsername(username: String): Boolean

}