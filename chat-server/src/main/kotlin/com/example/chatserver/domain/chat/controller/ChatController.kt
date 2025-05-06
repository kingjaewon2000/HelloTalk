package com.example.chatserver.domain.chat.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class ChatController {

    @MessageMapping("/rooms/{roomId}")
    fun handleMessage() {

    }

}