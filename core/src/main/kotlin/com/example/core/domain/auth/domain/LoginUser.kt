package com.example.core.domain.auth.domain

import java.io.Serializable

data class LoginUser(
    val userId: Long,
    val username: String
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

}