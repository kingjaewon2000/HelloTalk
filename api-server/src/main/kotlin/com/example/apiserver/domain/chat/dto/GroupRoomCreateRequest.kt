package com.example.apiserver.domain.chat.dto

data class GroupRoomCreateRequest(
    val roomName: String,
    val participantIds: List<Long>
)
