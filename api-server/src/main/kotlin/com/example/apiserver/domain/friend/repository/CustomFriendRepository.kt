package com.example.apiserver.domain.friend.repository

import com.example.apiserver.domain.friend.dto.FriendInfoResponse
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.core.common.model.Cursor

interface CustomFriendRepository {

    fun findAllByFromUserId(
        userId: Long,
        status: FriendStatus,
        cursor: Cursor?,
        limit: Int
    ): MutableList<FriendInfoResponse>

}