package com.example.chatserver.domain.chat.listener

import com.example.chatserver.domain.chat.websocket.ChatWebSocketHandler
import com.example.core.global.constant.RedisConstants
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.OutboundChatMessage
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
) : StreamListener<String, MapRecord<String, String, String>> {

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    companion object {
        private const val CONSUMER_GROUP_NAME = "consumer-group"
        private const val MESSAGE_KEY = "message"
    }

    override fun onMessage(message: MapRecord<String, String, String>?) {
        message ?: throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)

        val recordId = message.id.value
        val messageJson = message.value[MESSAGE_KEY]

        if (messageJson.isNullOrBlank()) {
            acknowledgeMessage(recordId)

            return
        }

        try {
            acknowledgeMessage(recordId)
            val outboundMessage = parseOutboundMessage(messageJson)

            broadcastMessage(outboundMessage)
        } catch (_: Exception) {
        }
    }

    private fun acknowledgeMessage(recordId: String) {
        try {
            redisTemplate.opsForStream<String, String>()
                .acknowledge(RedisConstants.getOutboundStreamKey(instanceId), CONSUMER_GROUP_NAME, recordId)
        } catch (_: Exception) {
        }
    }

    private fun parseOutboundMessage(messageJson: String): OutboundChatMessage {
        return objectMapper.readValue<OutboundChatMessage>(messageJson)
    }

    private fun broadcastMessage(
        outboundMessage: OutboundChatMessage
    ) {
        outboundMessage.participantIds.forEach { receiveUserId ->
//            chatWebSocketHandler.sendMessageToUser(
//                receiveUserId,
//                outboundMessage.content
//            )
        }
    }
}