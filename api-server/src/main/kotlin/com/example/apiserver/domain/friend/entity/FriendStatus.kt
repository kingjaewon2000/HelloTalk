package com.example.apiserver.domain.friend.entity

enum class FriendStatus(
    val description: String
) {

    PENDING("대기"), ACCEPTED("수락"), REJECTED("거절"), BLOCKED("차단");
}