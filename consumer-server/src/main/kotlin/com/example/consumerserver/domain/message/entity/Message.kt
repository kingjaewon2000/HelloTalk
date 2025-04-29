package com.example.consumerserver.domain.message.entity

import jakarta.persistence.*

@Entity
@Table(name = "messages")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(nullable = false)
    val senderUserId: Long,
    val receiveUserId: Long,

    val content: String
)