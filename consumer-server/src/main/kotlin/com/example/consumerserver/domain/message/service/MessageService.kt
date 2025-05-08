package com.example.consumerserver.domain.message.service

import com.example.core.domain.chat.entity.Message
import com.example.core.domain.chat.repository.ChatRoomRepository
import com.example.core.domain.chat.repository.MessageRepository
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.InboundChatMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MessageService(
    private val chatRoomRepository: ChatRoomRepository,
    private val messageRepository: MessageRepository,
) {

    @Transactional
    fun saveMessage(chatMessage: InboundChatMessage) {
        val room = chatRoomRepository.findChatRoomById(chatMessage.roomId)
            ?: throw ApiException(ErrorCode.CHAT_ROOM_NOT_FOUND)

        val message = Message(
            roomId = chatMessage.roomId,
            senderUserId = chatMessage.senderUserId,
            content = chatMessage.content,
        )

        messageRepository.save(message)

        room.lastMessage = message
    }

}