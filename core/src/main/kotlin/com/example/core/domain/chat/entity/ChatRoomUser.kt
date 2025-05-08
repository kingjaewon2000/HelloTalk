package com.example.core.domain.chat.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "room_user")
@EntityListeners(AuditingEntityListener::class)
class ChatRoomUser(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(nullable = false)
    val roomId: Long,

    @Column(nullable = false)
    val userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    var lastReadMessage: Message? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),
)