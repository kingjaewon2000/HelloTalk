package com.example.apiserver.domain.friend.repository

import com.example.apiserver.domain.friend.dto.FriendInfoInitial
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.core.global.model.Cursor

interface CustomFriendRepository {

    fun findAllByFromUserId(
        userId: Long,
        status: FriendStatus,
        cursor: Cursor?,
        limit: Int
    ): MutableList<FriendInfoInitial>

}