package com.example.consumerserver.domain.notification.service

import com.example.core.common.exception.ApiException
import com.example.core.common.exception.ErrorCode.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class NotificationServiceImpl : NotificationService<String> {

    override fun sendNotification(userId: Long, payload: String) {
        simulateSendNotification(100, 200)
    }

    override fun sendNotificationToMultiple(userIds: List<Long>, payload: String) {
        simulateSendNotification(1000, 2000)
    }

    private fun simulateSendNotification(start: Long, end: Long) {
        val time = Random.nextLong(start, end)

        try {
            Thread.sleep(time)
        } catch (e: Exception) {
            throw ApiException(INTERNAL_SERVER_ERROR)
        }
    }
}