package com.example.chatserver.global.principal

import java.security.Principal

class StompPrincipal(
    val userId: Long,
    private val username: String,
) : Principal {

    override fun getName(): String {
        return username
    }

}