package com.example.apiserver.domain.friend.entity

import com.example.apiserver.global.audit.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "friends")
class Friend(

    @Id
    @GeneratedValue
    var id: Long = 0L,

    @Column(nullable = false)
    val fromUserID: Long,

    @Column(nullable = false)
    val toUserId: Long,

    @Column(nullable = false)
    val requesterUserId: Long,

    @Enumerated(EnumType.STRING)
    val status: FriendStatus,

) : BaseTimeEntity()