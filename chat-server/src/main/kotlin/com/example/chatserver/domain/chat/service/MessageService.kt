package com.example.chatserver.domain.chat.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val messagingTemplate: SimpMessagingTemplate,
) {

    companion object {
        private const val DESTINATION = "/sub/rooms"
    }

    fun broadcastMessage(roomId: Long, payload: String) {
        val destination = "${DESTINATION}/${roomId}"
        messagingTemplate.convertAndSend(destination, payload)
    }

}