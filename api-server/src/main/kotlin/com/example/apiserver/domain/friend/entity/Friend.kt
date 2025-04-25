package com.example.apiserver.domain.friend.entity

import com.example.apiserver.domain.user.entity.User
import com.example.apiserver.global.audit.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "friends")
class Friend(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    val fromUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    val toUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    val requesterUser: User,

    @Enumerated(EnumType.STRING)
    val status: FriendStatus,

    ) : BaseTimeEntity()