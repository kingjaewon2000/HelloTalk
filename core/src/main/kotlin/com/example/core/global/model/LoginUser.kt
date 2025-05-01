package com.example.core.global.model

import java.io.Serializable

data class LoginUser(
    val userId: Long,
    val username: String
) : Serializable {

    companion object {
        private const val serialVersionUID = 4L
    }

}