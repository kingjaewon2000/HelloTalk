package com.example.chatserver.domain.chat.manager

import com.example.core.common.constant.RedisConstants
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class UserConnectionRegistry(
    private val redisTemplate: StringRedisTemplate,
) {

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    private fun getUserConnectionKey(userId: String): String = "${RedisConstants.USER_CONNECTION_KEY}:${userId}"

    fun addUserConnection(userId: String, instanceId: String): Boolean {
        val key = getUserConnectionKey(userId)

        return try {
            redisTemplate.opsForSet().add(key, instanceId)

            true
        } catch (_: Exception) {
            false
        }
    }

    fun removeUserConnection(userId: String, instanceId: String): Boolean {
        val key = getUserConnectionKey(userId)

        return try {
            val count = redisTemplate.opsForSet().remove(key, instanceId)

            if (count != null && count > 0) {
                val size = redisTemplate.opsForSet().size(key)

                if (size == 0.toLong()) {
                    redisTemplate.delete(key)
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    fun cleanup(userIds: List<String>) {
        userIds.forEach { removeUserConnection(it, instanceId) }
    }

}