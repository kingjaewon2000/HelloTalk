package com.example.chatserver.domain.chat.dto

data class ClientSentMessage(
    val roomId: Long,
    val content: String
)