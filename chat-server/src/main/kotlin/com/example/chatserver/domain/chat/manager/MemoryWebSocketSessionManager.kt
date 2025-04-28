package com.example.chatserver.domain.chat.manager

import com.example.chatserver.global.config.RedisConstants.Companion.USER_INSTANCE_MAP_KEY
import com.example.core.domain.auth.domain.LoginUser
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class MemoryWebSocketSessionManager(
    private val redisTemplate: StringRedisTemplate
) : WebSocketSessionManager {

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    private val userSessionMap: ConcurrentHashMap<String, MutableSet<String>> = ConcurrentHashMap()
    private val sessions: ConcurrentHashMap<String, WebSocketSession> = ConcurrentHashMap()

    override fun registerSession(userId: String, session: WebSocketSession): Boolean {
        val userSession = userSessionMap.getOrDefault(userId, mutableSetOf())
        val sessionId = session.id

        if (userSession.contains(sessionId)) {
            userSession.remove(sessionId)
            val oldSession = sessions.remove(sessionId)
            closeSession(oldSession, CloseStatus.POLICY_VIOLATION.withReason("New connection established"))
        }

        userSession.add(sessionId)
        sessions[sessionId] = session

        try {
            redisTemplate.opsForHash<String, String>().put(USER_INSTANCE_MAP_KEY, userId, instanceId)
        } catch (e: Exception) {
            throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
        }

        return true
    }

    override fun removeSession(session: WebSocketSession) {
        val sessionId = session.id
        val loginUser = session.attributes["LOGIN_USER"] as? LoginUser
        val userId = loginUser?.userId as String?

        sessions.remove(sessionId)

        if (userId == null) {
            return
        }

        userSessionMap[sessionId]?.remove(userId)

        try {
            redisTemplate.opsForHash<String, String>().delete(USER_INSTANCE_MAP_KEY, userId)
        } catch (e: Exception) {
            throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
        }

        closeSession(session, CloseStatus.NORMAL)
    }

    @PreDestroy
    override fun cleanup() {
        val userIds = userSessionMap.keys().toList()

        if (userIds.isNotEmpty()) {
            try {
                val redisMap = redisTemplate.opsForHash<String, String>().entries(USER_INSTANCE_MAP_KEY)

                val deleteEntries = redisMap.filter { (userId, mappedInstanceId) ->
                    mappedInstanceId == instanceId && userId in userIds
                }.map { it.key }

                redisTemplate.opsForHash<String, String>().delete(USER_INSTANCE_MAP_KEY, deleteEntries.toTypedArray())
            } catch (e: Exception) {
                throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }

        sessions.values.forEach {
            closeSession(it, CloseStatus.GOING_AWAY)
        }

        userSessionMap.clear()
        sessions.clear()
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

}