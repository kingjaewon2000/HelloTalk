package com.example.chatserver.domain.chat.listener

import com.example.chatserver.domain.chat.manager.UserConnectionRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener(
    private val userConnectionRegistry: UserConnectionRegistry
) {

    companion object {
        private const val USER_ID: String = "userId"
    }

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    @EventListener
    fun handleSessionConnectEvent(event: SessionConnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val sessionAttributes = headerAccessor.sessionAttributes ?: return
        val userId = sessionAttributes[USER_ID] as Long

        userConnectionRegistry.addUserConnection(userId.toString(), instanceId)
    }

    @EventListener
    fun handleSessionDisconnectEvent(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val sessionAttributes = headerAccessor.sessionAttributes ?: return
        val userId = sessionAttributes[USER_ID].toString()

        if (userId.isBlank()) {
            return
        }

        userConnectionRegistry.removeUserConnection(userId, instanceId)
    }

}