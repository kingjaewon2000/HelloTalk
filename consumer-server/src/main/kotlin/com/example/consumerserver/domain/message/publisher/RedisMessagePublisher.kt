package com.example.consumerserver.domain.message.publisher

import com.example.core.global.constant.RedisConstants
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisMessagePublisher(
    private val redisTemplate: StringRedisTemplate
) : MessagePublisher {

    override fun publish(instanceId: String, message: String) {
        try {
            val record =
                MapRecord.create(RedisConstants.getOutboundStreamKey(instanceId), mapOf("message" to message))
            val recordId = redisTemplate.opsForStream<String, String>().add(record)
            redisTemplate.opsForStream<String, String>()
                .acknowledge(RedisConstants.getOutboundStreamKey(instanceId), "consumer-group", recordId)
        } catch (_: Exception) {
        }
    }

}