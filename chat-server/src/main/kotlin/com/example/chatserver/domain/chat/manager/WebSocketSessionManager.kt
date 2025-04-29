package com.example.chatserver.domain.chat.manager

import org.springframework.web.socket.WebSocketSession

interface WebSocketSessionManager {

    fun registerSession(key: String, session: WebSocketSession): Boolean
    fun removeSession(session: WebSocketSession)

    fun cleanup()

    fun getSessionsByKey(key: String): List<WebSocketSession>

}