package com.example.consumerserver.domain.notification.service

interface NotificationService<T> {

    fun sendNotification(userId: Long, payload: T)
    fun sendNotificationToMultiple(userIds: Set<Long>, payload: T)

}