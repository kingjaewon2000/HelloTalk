package com.example.core.global.model

import java.io.Serializable

data class InboundChatMessage(
    val senderUserId: Long,
    val receiveUserId: Long,
    val content: String
) : Serializable {

    companion object {
        private const val serialVersionUID = 5L
    }

}