package com.example.chatserver.domain.chat.websocket

import com.example.chatserver.domain.chat.dto.ChatMessage
import com.example.chatserver.domain.chat.manager.WebSocketSessionManager
import com.example.chatserver.global.config.RedisConstants
import com.example.core.domain.auth.domain.LoginUser
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
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
        val userId = loginUser.userId.toString()

        val payload = message.payload as String

        val chatMessage = ChatMessage(
            senderUserId = userId,
            content = payload,
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

}