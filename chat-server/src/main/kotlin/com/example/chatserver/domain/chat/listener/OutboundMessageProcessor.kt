package com.example.chatserver.domain.chat.listener

import com.example.chatserver.domain.chat.websocket.ChatWebSocketHandler
import com.example.core.global.model.OutboundChatMessage
import com.example.core.global.constant.RedisConstants
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component

@Component
class OutboundMessageProcessor(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    private val chatWebSocketHandler: ChatWebSocketHandler
) : StreamListener<String, MapRecord<String, String, String>> {

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    override fun onMessage(message: MapRecord<String, String, String>?) {
        message ?: throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)

        val recordId = message.id.value
        val messageJson = message.value["message"]

        if (messageJson.isNullOrBlank()) throw ApiException(ErrorCode.BAD_REQUEST)

        try {
            redisTemplate.opsForStream<String, String>()
                .acknowledge(RedisConstants.getOutboundStreamKey(instanceId), "consumer-group", recordId)
            val chatMessage = objectMapper.readValue<OutboundChatMessage>(messageJson)
            chatWebSocketHandler.sendMessageToUser(chatMessage.receiveUserId, chatMessage.content)
        } catch (_: Exception) {
        }
    }

}
