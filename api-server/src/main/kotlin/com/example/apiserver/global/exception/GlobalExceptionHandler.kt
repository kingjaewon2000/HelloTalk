package com.example.apiserver.global.exception

import com.example.core.global.api.ApiExceptionResponse
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiExceptionResponse> {
        val errorCode = ErrorCode.UN_SUPPORTED_OPERATION

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiExceptionResponse.of(
                    errorCode.status,
                    errorCode.message
                )
            )
    }

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