package com.example.core.global.model

import java.io.Serializable

data class InboundChatMessage(
    val roomId: Long,
    val senderUserId: Long,
    val content: String
) : Serializable {

    companion object {
        private const val serialVersionUID = 5L
    }

}