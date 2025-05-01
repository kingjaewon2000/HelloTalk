package com.example.consumerserver.domain.message.service

import com.example.consumerserver.domain.message.repository.MessageRepository
import com.example.core.global.model.InboundChatMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MessageService(
    private val messageRepository: MessageRepository,
) {

    @Transactional
    fun saveMessage(chatMessage: InboundChatMessage) {
//        val message = Message(
//        )
//
//        messageRepository.save(message)
    }

}