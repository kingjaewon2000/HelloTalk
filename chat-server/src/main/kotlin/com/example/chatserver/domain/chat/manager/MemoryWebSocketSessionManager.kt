package com.example.chatserver.domain.chat.manager

import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.LoginUser
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class MemoryWebSocketSessionManager(
    private val userConnectionRegistry: UserConnectionRegistry,
) : WebSocketSessionManager {

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    // key: userId value: sessionId
    private val userSessionMap: ConcurrentHashMap<String, MutableSet<String>> = ConcurrentHashMap()

    // key: sessionId value: session
    private val sessionMap: ConcurrentHashMap<String, WebSocketSession> = ConcurrentHashMap()

    override fun registerSession(userId: String, session: WebSocketSession): Boolean {
        val userSession = userSessionMap.getOrPut(userId) {
            mutableSetOf()
        }
        val sessionId = session.id

        if (userSession.contains(sessionId)) {
            userSession.remove(sessionId)
            val oldSession = sessionMap.remove(sessionId)
            closeSession(oldSession, CloseStatus.POLICY_VIOLATION.withReason("New connection established"))
        }

        userSession.add(sessionId)
        sessionMap[sessionId] = session

        return userConnectionRegistry.addUserConnection(userId, instanceId)
    }

    override fun removeSession(session: WebSocketSession) {
        val sessionId = session.id
        val loginUser = session.attributes["LOGIN_USER"] as? LoginUser
        val userId = loginUser?.userId.toString()

        sessionMap.remove(sessionId)

        userSessionMap[sessionId]?.remove(userId)

        try {
            userConnectionRegistry.removeUserConnection(userId, instanceId)
        } catch (e: Exception) {
            throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
        }

        closeSession(session, CloseStatus.NORMAL)
    }

    private fun closeSession(session: WebSocketSession?, closeStatus: CloseStatus) {
        session?.let {
            try {
                if (it.isOpen) {
                    it.close(closeStatus)
                }
            } catch (e: Exception) {
                throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PreDestroy
    override fun cleanup() {
        val userIds = userSessionMap.keys().toList()

        if (userIds.isNotEmpty()) {
            try {
                userConnectionRegistry.cleanup(userIds)
            } catch (e: Exception) {
                throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }

        sessionMap.values.forEach {
            closeSession(it, CloseStatus.GOING_AWAY)
        }

        userSessionMap.clear()
        sessionMap.clear()
    }

    override fun getSessionsByKey(key: String): List<WebSocketSession> {
        val sessionIds = userSessionMap[key] ?: throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
        val sessions = sessionIds.mapNotNull { sessionMap[it] }.toList()

        return sessions
    }

}