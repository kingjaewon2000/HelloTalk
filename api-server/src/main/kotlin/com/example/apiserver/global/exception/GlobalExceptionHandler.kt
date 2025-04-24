package com.example.apiserver.global.exception

import com.example.core.global.api.ApiExceptionResponse
import com.example.core.global.exception.ApiException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleException(e: ApiException): ResponseEntity<ApiExceptionResponse> {
        val errorCode = e.errorCode

        return ResponseEntity
            .status(errorCode.status)
            .body(
                ApiExceptionResponse.of(
                    errorCode.status,
                    errorCode.message
                )
            )
    }
}