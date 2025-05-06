package com.example.chatserver.domain.chat.websocket

import com.example.chatserver.domain.chat.dto.ClientSentMessage
import com.example.chatserver.domain.chat.manager.WebSocketSessionManager
import com.example.core.domain.chat.repository.ChatRoomUserRepository
import com.example.core.global.constant.RedisConstants.Companion.INBOUND_STREAM_KEY
import com.example.core.global.constant.SessionConstants.Companion.LOGIN_USER_ATTRIBUTE
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.InboundChatMessage
import com.example.core.global.model.LoginUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

//@Component
class ChatWebSocketHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val redisTemplate: StringRedisTemplate,
    private val chatRoomUserRepository: ChatRoomUserRepository,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {


    override fun afterConnectionEstablished(session: WebSocketSession) {
        val loginUser = getLoginUserFromSession(session)
        val userId = loginUser.userId.toString()

        webSocketSessionManager.registerSession(userId, session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        webSocketSessionManager.removeSession(session)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR)
    }

    /**
     * 클라이언트로부터 수신된 WebSocket 텍스트 메시지를 처리하는 기본 핸들러입니다.
     * 이 메서드는 다음 주요 단계를 수행합니다:
     * 1. WebSocket 세션에서 현재 로그인한 사용자 정보를 가져옵니다.
     * 2. 수신된 텍스트 메시지의 페이로드(JSON 문자열)를 파싱하여 클라이언트가 보낸 메시지 객체(ClientSentMessage)로 변환합니다.
     * 3. 로그인한 유저가(senderUserId) 채팅방에 참여하고 있는 유저인지 확인합니다.
     * 4. 내부 시스템에서 사용할 메시지 형식(InboundChatMessage)으로 변환합니다. (발신자 ID 포함)
     * 5. 내부 메시지 객체를 다시 JSON 문자열로 직렬화합니다.
     * 6. 최종적으로 직렬화된 메시지를 다른 처리 시스템(Redis Stream)으로 발행(publish)합니다.
     *
     * @param session 메시지를 수신한 WebSocket 세션 객체.
     * @param message 클라이언트로부터 받은 텍스트 기반의 WebSocket 메시지.
     */
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val loginUser = getLoginUserFromSession(session)
            val senderUserId = loginUser.userId

            val sentMessage = parseClientSentMessage(message.payload)

            if (isNotUserInRoom(sentMessage.roomId, senderUserId)) {
                throw ApiException(ErrorCode.BAD_REQUEST)
            }

            val inboundMessage = createInboundMessage(senderUserId, sentMessage)
            val messageJson = serializeToJson(inboundMessage)

            publishMessage(messageJson)
        } catch (_: Exception) {
        }
    }

    private fun getLoginUserFromSession(session: WebSocketSession): LoginUser {
        return session.attributes[LOGIN_USER_ATTRIBUTE] as? LoginUser
            ?: run {
                throw ApiException(ErrorCode.UNAUTHORIZED)
            }
    }

    private fun parseClientSentMessage(message: String): ClientSentMessage {
        return objectMapper.readValue<ClientSentMessage>(message)
    }

    private fun isNotUserInRoom(roomId: Long, senderUserId: Long): Boolean {
        return !chatRoomUserRepository.existsByRoomIdAndUserId(roomId, senderUserId)
    }

    private fun createInboundMessage(senderUserId: Long, message: ClientSentMessage): InboundChatMessage {
        return InboundChatMessage(
            roomId = message.roomId,
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

    /**
     * 특정 사용자 ID에게 WebSocket 메시지를 전송합니다.
     * 해당 사용자가 여러 세션(예: 다른 기기나 브라우저 탭)에 연결되어 있을 경우,
     * 연결된 모든 세션에 메시지를 전달합니다.
     * 메시지 전송 중 예외가 발생하면 해당 세션을 관리자에서 제거합니다.
     *
     * @param userId 메시지를 수신할 사용자의 ID
     * @param message 전송할 메시지 내용 (문자열)
     */
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