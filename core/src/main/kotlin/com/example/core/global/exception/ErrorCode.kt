package com.example.core.global.exception

enum class ErrorCode(val status: Int, val message: String) {

    // 유저 에러
    USER_ALREADY_EXISTS(400, "이미 존재하는 아이디입니다."),

    // 공통 에러
    BAD_REQUEST(400, "잘못된 요청 파라미터입니다."),
    NOT_FOUND(404, "요청하신 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부에 오류가 발생했습니다."),

    // 인증 / 인가 에러
    UNAUTHORIZED(401, "인증되지 않은 사용자입니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),

}