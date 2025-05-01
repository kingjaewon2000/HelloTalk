package com.example.apiserver.domain.chat.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "rooms")
@EntityListeners(AuditingEntityListener::class)
class ChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Enumerated(EnumType.STRING)
    val type: RoomType,

    val name: String,

    val lastActivityAt: LocalDateTime,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)