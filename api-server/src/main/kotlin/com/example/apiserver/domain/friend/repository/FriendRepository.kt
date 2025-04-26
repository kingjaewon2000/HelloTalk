package com.example.apiserver.domain.friend.repository

import com.example.apiserver.domain.friend.entity.Friend
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<Friend, Long>, CustomFriendRepository {

    fun existsByFromUserIdAndToUserId(fromUserId: Long, toUserId: Long): Boolean

}