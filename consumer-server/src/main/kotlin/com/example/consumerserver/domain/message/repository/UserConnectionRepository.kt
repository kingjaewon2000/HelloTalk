package com.example.consumerserver.domain.message.repository

import com.example.core.global.constant.RedisConstants
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class UserConnectionRepository(
    private val redisTemplate: StringRedisTemplate
) {

    private fun getUserConnectionKey(userId: Long): String = "${RedisConstants.USER_CONNECTION_KEY}:${userId}"

    fun getUserConnectedInstances(participantIds: Set<Long>): Set<String> {
        if (participantIds.isEmpty()) {
            return emptySet()
        }

        val keys = participantIds.map { getUserConnectionKey(it) }

        return try {
            val instanceIds: Set<String>? = redisTemplate.opsForSet().union(keys)

            instanceIds ?: emptySet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun getOnlineUserIds(participantIds: Set<Long>): Set<Long> {
        if (participantIds.isEmpty()) {
            return emptySet()
        }

        return try {
            val onlineUserIds = mutableSetOf<Long>()

            participantIds.forEach { participantId ->
                val key = getUserConnectionKey(participantId)

                val size = redisTemplate.opsForSet().size(key)
                if (size != 0.toLong()) onlineUserIds.add(participantId)
            }

            onlineUserIds
        } catch (_: Exception) {
            emptySet()
        }
    }

}