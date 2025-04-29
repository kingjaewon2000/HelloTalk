package com.example.core.common.exception

class ApiException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)