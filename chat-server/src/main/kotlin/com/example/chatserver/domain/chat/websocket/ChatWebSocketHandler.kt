package com.example.chatserver.domain.chat.websocket

import com.example.chatserver.domain.chat.dto.ClientSentMessage
import com.example.chatserver.domain.chat.manager.WebSocketSessionManager
import com.example.core.common.model.LoginUser
import com.example.core.common.model.InboundChatMessage
import com.example.core.common.constant.RedisConstants
import com.example.core.common.exception.ApiException
import com.example.core.common.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ChatWebSocketHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {


    override fun afterConnectionEstablished(session: WebSocketSession) {
        val loginUser = session.attributes["LOGIN_USER"] as? LoginUser ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        val userId = loginUser.userId.toString()

        webSocketSessionManager.registerSession(userId, session)
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val loginUser = session.attributes["LOGIN_USER"] as? LoginUser ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        val senderUserId = loginUser.userId

        val payload = message.payload as String
        val sentMessage = objectMapper.readValue<ClientSentMessage>(payload)

        val chatMessage = InboundChatMessage(
            senderUserId = senderUserId,
            receiveUserId = sentMessage.receiveUserId,
            content = sentMessage.content,
        )

        val messageJson = objectMapper.writeValueAsString(chatMessage)

        try {
            val record = MapRecord.create(RedisConstants.INBOUND_STREAM_KEY, mapOf("message" to messageJson))
            val recordId: RecordId = redisTemplate.opsForStream<String, String>().add(record) ?: throw ApiException(
                ErrorCode.INTERNAL_SERVER_ERROR
            )
        } catch (e: Exception) {
            throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        webSocketSessionManager.removeSession(session)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR)
    }

    fun sendMessageToUser(userId: Long, message: String) {
        val sessions = webSocketSessionManager.getSessionsByKey(userId.toString())

        sessions.forEach { session ->
            try {
                val textMessage = TextMessage(message)
                session.sendMessage(textMessage)
            } catch (e: Exception) {
                webSocketSessionManager.removeSession(session)
            }
        }
    }

}