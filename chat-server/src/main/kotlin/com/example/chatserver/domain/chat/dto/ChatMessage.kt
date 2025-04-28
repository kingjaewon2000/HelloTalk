package com.example.chatserver.domain.chat.dto

data class ChatMessage(
    val senderUserId: String,
    val content: String
)
