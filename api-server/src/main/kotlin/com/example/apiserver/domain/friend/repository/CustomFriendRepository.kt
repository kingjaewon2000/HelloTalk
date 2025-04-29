package com.example.apiserver.domain.friend.repository

import com.example.apiserver.domain.friend.dto.FriendInfoResponse
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.core.common.model.CursorInfo

interface CustomFriendRepository {

    fun findAllByFromUserId(
        userId: Long,
        status: FriendStatus,
        cursorInfo: CursorInfo?,
        limit: Int
    ): MutableList<FriendInfoResponse>

}