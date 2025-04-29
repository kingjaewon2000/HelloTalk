package com.example.chatserver.domain.chat.dto

data class ClientSentMessage(
    val receiveUserId: Long,
    val content: String
)