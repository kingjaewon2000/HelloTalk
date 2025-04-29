package com.example.core.common.api

/*
 * API 공통 에러 응답
 */
class ApiExceptionResponse(
    val status: Int,
    val message: String
) {

    companion object {
        fun of(status: Int, message: String) = ApiExceptionResponse(status, message)
    }

}