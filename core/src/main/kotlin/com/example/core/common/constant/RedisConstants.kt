package com.example.core.common.constant

class RedisConstants {

    companion object {
        const val INBOUND_STREAM_KEY = "chat:messages:inbound"
        private const val OUTBOUND_STREAM_KEY = "chat:messages:outbound"
        const val USER_CONNECTION_KEY = "user:connections"

        fun getOutboundStreamKey(instanceId: String) = "$OUTBOUND_STREAM_KEY:${instanceId}"
    }

}