package com.baseball.record.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    SIGNUP_REQUIRED(HttpStatus.NOT_FOUND, "가입되지 않은 사용자입니다. 회원가입이 필요합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고입니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고 대상입니다."),
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 다이어리입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 다이어리에 작성된 리뷰가 있습니다."),
    RECORD_HAS_REVIEW(HttpStatus.CONFLICT, "리뷰가 작성된 다이어리는 삭제할 수 없습니다."),
    INVALID_LIMIT(HttpStatus.BAD_REQUEST, "limit은 1 이상이어야 합니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 사용자입니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 provider입니다."),
    SOCIAL_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "소셜 계정의 이메일 제공에 동의해야 가입할 수 있습니다."),
    INVALID_PROVIDER_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 소셜 로그인 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    MEMBER_NOT_ACTIVE(HttpStatus.FORBIDDEN, "탈퇴했거나 이용이 제한된 계정입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
