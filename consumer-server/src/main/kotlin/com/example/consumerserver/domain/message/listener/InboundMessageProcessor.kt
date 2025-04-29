package com.example.consumerserver.domain.message.listener

import com.example.consumerserver.domain.message.service.MessageService
import com.example.consumerserver.domain.message.publisher.RedisMessagePublisher
import com.example.consumerserver.domain.message.repository.UserConnectionRepository
import com.example.consumerserver.domain.notification.service.NotificationService
import com.example.core.common.model.InboundChatMessage
import com.example.core.common.constant.RedisConstants.Companion.INBOUND_STREAM_KEY
import com.example.core.common.exception.ApiException
import com.example.core.common.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component

@Component
class InboundMessageProcessor(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    private val redisMessagePublisher: RedisMessagePublisher,
    private val userConnectionRepository: UserConnectionRepository,
    private val messageService: MessageService,
    private val notificationService: NotificationService<String>
) : StreamListener<String, MapRecord<String, String, String>> {

    override fun onMessage(message: MapRecord<String, String, String>?) {
        message ?: throw ApiException(ErrorCode.INTERNAL_SERVER_ERROR)

        val recordId = message.id
        val messageJson = message.value["message"]

        if (messageJson.isNullOrBlank()) throw ApiException(ErrorCode.BAD_REQUEST)

        try {
            val chatMessage = objectMapper.readValue<InboundChatMessage>(messageJson)
            messageService.saveMessage(chatMessage)

            redisTemplate.opsForStream<String, String>().acknowledge(INBOUND_STREAM_KEY, "consumer-group", recordId)

            val instanceIds = userConnectionRepository.getUserConnectedInstances(chatMessage.receiveUserId)
            if (isOnline(instanceIds)) {
                for (instanceId in instanceIds!!) {
                    redisMessagePublisher.publish(instanceId, messageJson)
                }
            } else {
                notificationService.sendNotification(userId = chatMessage.receiveUserId, "메시지가 도착했습니다.")
            }
        } catch (_: Exception) {
        }
    }

    private fun isOnline(instanceIds: Set<String>?): Boolean {
        return !instanceIds.isNullOrEmpty()
    }

}