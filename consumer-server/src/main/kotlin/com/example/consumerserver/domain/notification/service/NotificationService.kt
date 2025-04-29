package com.example.consumerserver.domain.notification.service

interface NotificationService<T> {

    fun sendNotification(userId: Long, payload: T)
    fun sendNotificationToMultiple(userIds: List<Long>, payload: T)

}