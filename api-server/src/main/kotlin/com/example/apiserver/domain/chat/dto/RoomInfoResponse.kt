package com.example.apiserver.domain.chat.dto

import com.example.apiserver.domain.chat.entity.RoomType
import java.time.LocalDateTime

data class RoomInfoResponse(
    val roomId: Long,
    val type: RoomType,
    val roomName: String,
//    val lastMessageContent: String,
    val lastActivityAt: LocalDateTime,
)