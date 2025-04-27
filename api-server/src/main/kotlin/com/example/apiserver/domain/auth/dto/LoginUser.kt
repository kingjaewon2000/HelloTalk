package com.example.apiserver.domain.auth.dto

import java.io.Serializable

data class LoginUser(
    val id: Long,
    val username: String
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

}