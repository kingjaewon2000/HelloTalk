package com.example.chatserver.domain.chat.listener

import com.example.chatserver.domain.chat.manager.UserConnectionRegistry
import com.example.chatserver.global.principal.StompPrincipal
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
        val userId = sessionAttributes["userId"] as Long
        val username = sessionAttributes["username"] as String

        val principal = StompPrincipal(userId, username)
        headerAccessor.user = principal

        userConnectionRegistry.addUserConnection(userId.toString(), instanceId)
    }

    @EventListener
    fun handleSessionDisconnectEvent(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val user = headerAccessor.user as StompPrincipal
        val userId = user.userId

        userConnectionRegistry.removeUserConnection(userId.toString(), instanceId)
    }

}