package com.baseball.record.domain.member;

public enum MemberStatus {

    /**
     * 정상 활동 중인 계정.
     */
    ACTIVE,

    /**
     * 회원 탈퇴(자진) 처리된 계정. 즉시 삭제 대신 소프트 삭제로 상태만 전환한다.
     */
    INACTIVE,

    /**
     * 관리자에 의해 악성 유저로 판단되어 이용이 제한된 계정.
     */
    SUSPENDED
}
