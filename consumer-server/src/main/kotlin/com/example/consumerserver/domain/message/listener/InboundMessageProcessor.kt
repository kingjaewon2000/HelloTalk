package com.example.consumerserver.domain.message.listener

import com.example.consumerserver.domain.message.publisher.RedisMessagePublisher
import com.example.consumerserver.domain.message.repository.UserConnectionRepository
import com.example.consumerserver.domain.message.service.MessageService
import com.example.consumerserver.domain.notification.service.NotificationService
import com.example.core.domain.chat.repository.ChatRoomUserRepository
import com.example.core.global.constant.RedisConstants.Companion.INBOUND_STREAM_KEY
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.InboundChatMessage
import com.example.core.global.model.OutboundChatMessage
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component

@Component
class InboundMessageProcessor(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    private val redisMessagePublisher: RedisMessagePublisher,
    private val messageService: MessageService,
    private val chatRoomUserRepository: ChatRoomUserRepository,
    private val userConnectionRepository: UserConnectionRepository,
    private val notificationService: NotificationService<String>
) : StreamListener<String, MapRecord<String, String, String>> {

    companion object {
        private const val CONSUMER_GROUP_NAME = "consumer-group"
        private const val NOTIFICATION_MESSAGE = "메시지가 도착했습니다."
        private const val MESSAGE_KEY = "message"
    }

    override fun onMessage(message: MapRecord<String, String, String>?) {
        message ?: throw ApiException(ErrorCode.BAD_REQUEST)

        val recordId = message.id
        val messageJson = message.value[MESSAGE_KEY]

        if (messageJson.isNullOrBlank()) {
            acknowledgeMessage(recordId)

            return
        }

        try {
            val inboundMessage = parseInboundMessage(messageJson)
            messageService.saveMessage(inboundMessage)

            publishMessage(inboundMessage)
            acknowledgeMessage(recordId)
        } catch (_: Exception) {
        }
    }

    private fun acknowledgeMessage(recordId: RecordId) {
        try {
            redisTemplate.opsForStream<String, String>().acknowledge(INBOUND_STREAM_KEY, CONSUMER_GROUP_NAME, recordId)
        } catch (_: Exception) {
        }
    }

    private fun publishMessage(inboundMessage: InboundChatMessage) {
        val participantIds = chatRoomUserRepository.findUserIdsByRoomId(inboundMessage.roomId)

        if (participantIds.isEmpty()) {
            return
        }

        val outboundMessage = OutboundChatMessage(
            roomId = inboundMessage.roomId,
            participantIds = participantIds,
            senderUserId = inboundMessage.senderUserId,
            content = inboundMessage.content,
        )

        try {
            val outboundMessageJson = objectMapper.writeValueAsString(outboundMessage)

            sendByOnlineStatus(participantIds, outboundMessageJson)
        } catch (e: JsonProcessingException) {
            throw ApiException(ErrorCode.BAD_REQUEST)
        }
    }

    private fun parseInboundMessage(messageJson: String): InboundChatMessage {
        return objectMapper.readValue<InboundChatMessage>(messageJson)
    }

    private fun sendByOnlineStatus(participantIds: Set<Long>, outboundMessageJson: String) {
        val connectedInstanceIds = userConnectionRepository.getUserConnectedInstances(participantIds)
        val onlineUserIds = userConnectionRepository.getOnlineUserIds(participantIds)
        val offlineUserIds = participantIds - onlineUserIds


        if (connectedInstanceIds.isEmpty()) {
            sendOfflineNotifications(offlineUserIds)
            println(offlineUserIds)
        } else {
            publishToOnlineInstances(connectedInstanceIds, outboundMessageJson)
        }
    }

    private fun sendOfflineNotifications(offlineUserIds: Set<Long>) {
        notificationService.sendNotificationToMultiple(userIds = offlineUserIds, NOTIFICATION_MESSAGE)
    }

    private fun publishToOnlineInstances(instanceIds: Set<String>, outboundMessageJson: String) {
        instanceIds.forEach { instanceId ->
            try {
                redisMessagePublisher.publish(instanceId, outboundMessageJson)
            } catch (_: Exception) {
            }
        }
    }

}