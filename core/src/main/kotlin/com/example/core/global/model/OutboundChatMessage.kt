package com.example.core.global.model

import java.io.Serializable

class OutboundChatMessage(
    val roomId: Long,
    val senderUserId: Long,
    val participantIds: Set<Long>,
    val content: String
) : Serializable {

    companion object {
        private const val serialVersionUID = 5L
    }

}