package com.example.core.global.model

import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.exception.ErrorCode.UN_SUPPORTED_OPERATION

/*
 * 커서 기반 페이징을 위한 커서 정보를 담는 클래
 * 정렬 기준 키 값들을 순서대로 저장합니다.
 */
class Cursor private constructor(
    private val values: List<String>
) {

    operator fun component1(): String = this.values.getOrNull(0) ?: throw ApiException(UN_SUPPORTED_OPERATION)
    operator fun component2(): String = this.values.getOrNull(1) ?: throw ApiException(UN_SUPPORTED_OPERATION)
    operator fun component3(): String = this.values.getOrNull(2) ?: throw ApiException(UN_SUPPORTED_OPERATION)
    operator fun component4(): String = this.values.getOrNull(3) ?: throw ApiException(UN_SUPPORTED_OPERATION)
    operator fun component5(): String = this.values.getOrNull(4) ?: throw ApiException(UN_SUPPORTED_OPERATION)

    companion object {
        const val DELIMITER = "_"
        private const val MAX_SIZE = 5

        fun decode(cursorId: String?, expectedKeyCount: Int? = null): Cursor? {
            if (cursorId.isNullOrBlank()) return null

            return try {
                val parts = cursorId.split(DELIMITER)

                if (parts.size > MAX_SIZE) throw ApiException(ErrorCode.BAD_REQUEST)

                if (expectedKeyCount != null && parts.size != expectedKeyCount) return null

                Cursor(parts)
            } catch (e: Exception) {
                throw ApiException(ErrorCode.BAD_REQUEST)
            }
        }
    }

}
