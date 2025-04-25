package com.example.apiserver.domain.friend.repository

import com.example.apiserver.domain.friend.entity.Friend
import com.example.apiserver.domain.friend.entity.FriendStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FriendRepository : JpaRepository<Friend, Long> {

    fun existsByFromUserIdAndToUserId(fromUserId: Long, toUserId: Long): Boolean

    @Query("""
    SELECT f
    FROM Friend f
    JOIN FETCH f.toUser
    WHERE f.fromUser.id = :fromUserId
      AND f.status = :status
"""
    )
    fun findAllByFromUserId(
        @Param("fromUserId") fromUserId: Long,
        @Param("status") status: FriendStatus
    ): List<Friend>
}