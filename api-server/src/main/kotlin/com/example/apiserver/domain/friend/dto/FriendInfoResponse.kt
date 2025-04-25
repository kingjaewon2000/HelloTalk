package com.example.apiserver.domain.friend.dto

data class FriendInfoResponse(
    val friendId: Long,
    val userId: Long,
    val username: String,
    val name: String,
    val status: String
)
