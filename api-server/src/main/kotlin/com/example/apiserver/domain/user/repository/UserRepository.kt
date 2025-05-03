package com.example.apiserver.domain.user.repository

import com.example.apiserver.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {

    fun findUserById(userId: Long): User?
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean

    @Query("SELECT u.id FROM User u WHERE u.id IN :userIds")
    fun findByIdsIn(userIds: Set<Long>): Set<Long>
}
