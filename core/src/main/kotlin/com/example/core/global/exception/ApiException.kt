package com.example.core.global.exception

class ApiException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)