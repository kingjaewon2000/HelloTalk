package com.example.apiserver.domain.user.entity

import com.example.core.global.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    val name: String

) : BaseTimeEntity()