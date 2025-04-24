package com.example.core.global.api

/*
 * API 공통 응답
 */
sealed class ApiResponse<out T> {

    data class Success<T>(val status: Int, val data: T) : ApiResponse<T>()

    companion object {
        fun <T> success(data: T) = Success(200, data)

        fun <T> of(status: Int, data: T) = Success(status, data)
    }

}
