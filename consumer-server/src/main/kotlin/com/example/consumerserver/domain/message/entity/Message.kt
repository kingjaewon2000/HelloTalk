package com.example.consumerserver.domain.message.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "messages")
@EntityListeners(AuditingEntityListener::class)
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(nullable = false)
    val roomId: Long,

    @Column(nullable = false, updatable = false)
    val senderUserId: Long,

    val content: String,

    val createdAt: LocalDateTime
)