package com.example.core.common.exception

enum class ErrorCode(val status: Int, val message: String) {

    // 친구 에러
    NOT_ALLOW_SELF_ADD_FRIEND(400, "자기 자신을 친구 추가하는 것은 허용되지 않습니다."),
    NOT_ALLOW_ALREADY_ADDED_FRIEND(400, "이미 친구 추가가 된 사용자는 다시 추가할 수 없습니다."),

    // 유저 에러
    USER_ALREADY_EXISTS(400, "이미 존재하는 아이디입니다."),
    USER_NOT_FOUND(400, "요청하신 사용자를 찾을 수 없습니다."),

    // 채팅방 에러
    CHAT_ROOM_NOT_FOUND(400, "존재하지 않는 채팅방입니다."),
    NOT_ALLOWED_SELF_INVITATION(400, "자기 자신은 초대할 수 없습니다."),
    INVALID_GROUP_SIZE(400, "그룹 인원은 3명 이상 1000명 이하이어야 합니다."),

    // 공통 에러
    BAD_REQUEST(400, "잘못된 요청 파라미터입니다."),
    UN_SUPPORTED_OPERATION(400, "지원하지 않는 작업입니다."),
    NOT_FOUND(404, "요청하신 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부에 오류가 발생했습니다."),

    // 인증 / 인가 에러
    UNAUTHORIZED(401, "인증되지 않은 사용자입니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),

}