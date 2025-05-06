package com.example.chatserver.domain.chat.listener

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener {

    @EventListener
    fun handleSessionConnectedEventListen(event: SessionConnectedEvent) {
        println("connected!")
    }

    @EventListener
    fun handleSessionDisconnectEvent(event: SessionDisconnectEvent) {
        println("disconnected!")
    }

}