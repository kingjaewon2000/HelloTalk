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

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    @EventListener
    fun handleSessionConnectEvent(event: SessionConnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val sessionAttributes = headerAccessor.sessionAttributes ?: return
        val userId = sessionAttributes["userId"].toString()

        userConnectionRegistry.addUserConnection(userId, instanceId)
    }

    @EventListener
    fun handleSessionDisconnectEvent(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val sessionAttributes = headerAccessor.sessionAttributes ?: return
        val userId = sessionAttributes["userId"].toString()

        if (userId.isBlank()) {
            return
        }

        userConnectionRegistry.removeUserConnection(userId, instanceId)
    }

}