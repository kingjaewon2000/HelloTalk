package com.example.core.domain.chat.dto

import com.example.core.domain.chat.entity.RoomType
import java.time.LocalDateTime

data class RoomInfoResponse(
    val roomId: Long,
    val type: RoomType,
    val roomName: String,
//    val lastMessageContent: String,
    val lastActivityAt: LocalDateTime,
)