package com.example.core.common.api

/*
 * API 커서 기반 공통 응답
 */
data class ApiCursorResponse<T>(
    val hasNext: Boolean,
    val cursorId: String?,
    val data: List<T>
)