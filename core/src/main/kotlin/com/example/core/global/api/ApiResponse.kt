package com.example.core.global.api

/*
 * API 응답 상태와 데이터를 감싸는 제네릭 클래스
 */
sealed class ApiResponse<out T> {

    data class Success<T>(val status: Int, val data: T) : ApiResponse<T>()

    data class Error(val error: RuntimeException) : ApiResponse<Nothing>()

    companion object {
        fun <T> success(data: T): ApiResponse<T> = Success(200, data)

        fun error(error: RuntimeException): ApiResponse<Nothing> = Error(error)
    }

}
