package com.example.chatserver.domain.chat.controller

import com.example.chatserver.domain.chat.dto.ClientSentMessage
import com.example.core.domain.chat.repository.ChatRoomUserRepository
import com.example.core.global.constant.RedisConstants.Companion.INBOUND_STREAM_KEY
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.InboundChatMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val chatRoomUserRepository: ChatRoomUserRepository,
) {

    companion object {
        private const val USER_ID: String = "userId"
    }

    @MessageMapping("/rooms/{roomId}")
    fun handleMessage(
        @DestinationVariable roomId: Long,
        @Payload message: ClientSentMessage,
        headerAccessor: SimpMessageHeaderAccessor

    ) {
        try {
            val sessionAttributes = headerAccessor.sessionAttributes ?: return
            val senderUserId = sessionAttributes[USER_ID] as Long

            if (isNotUserInRoom(roomId, senderUserId)) {
                throw ApiException(ErrorCode.BAD_REQUEST)
            }

            val inboundMessage = createInboundMessage(senderUserId, roomId, message)
            val messageJson = serializeToJson(inboundMessage)

            publishMessage(messageJson)
        } catch (_: Exception) {
        }
    }

    private fun isNotUserInRoom(roomId: Long, senderUserId: Long): Boolean {
        return !chatRoomUserRepository.existsByRoomIdAndUserId(roomId, senderUserId)
    }

    private fun createInboundMessage(roomId: Long, senderUserId: Long, message: ClientSentMessage): InboundChatMessage {
        return InboundChatMessage(
            roomId = roomId,
            senderUserId = senderUserId,
            content = message.content,
        )
    }

    private fun serializeToJson(message: InboundChatMessage): String {
        return objectMapper.writeValueAsString(message)
    }

    private fun publishMessage(messageJson: String) {
        val record = MapRecord.create(INBOUND_STREAM_KEY, mapOf("message" to messageJson))

        redisTemplate.opsForStream<String, String>().add(record) ?: throw ApiException(
            ErrorCode.INTERNAL_SERVER_ERROR
        )
    }

}