package com.example.apiserver.domain.friend.dto

import com.example.apiserver.domain.friend.entity.FriendStatus

data class FriendInfoInitial(
    val friendId: Long,
    val userId: Long,
    val username: String,
    val name: String,
    val status: FriendStatus,
)

data class FriendInfoResponse(
    val friendId: Long,
    val userId: Long,
    val username: String,
    val name: String,
    val status: FriendStatus,
    val onlineStatus: Boolean
)