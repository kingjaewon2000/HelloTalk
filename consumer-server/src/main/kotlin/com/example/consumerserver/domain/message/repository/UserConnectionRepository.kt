package com.example.consumerserver.domain.message.repository

import com.example.core.global.constant.RedisConstants
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class UserConnectionRepository(
    private val redisTemplate: StringRedisTemplate
) {

    private fun getUserConnectionKey(userId: Long): String = "${RedisConstants.USER_CONNECTION_KEY}:${userId}"

    fun getUserConnectedInstances(userId: Long): Set<String>? {
        val key = getUserConnectionKey(userId)

        return try {
            redisTemplate.opsForSet().members(key) ?: emptySet()
        } catch (_: Exception) {
            null
        }
    }

}