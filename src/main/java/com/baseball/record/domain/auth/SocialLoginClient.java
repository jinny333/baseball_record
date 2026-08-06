package com.baseball.record.domain.auth;

import com.baseball.record.domain.member.Provider;

public interface SocialLoginClient {

    boolean supports(Provider provider);

    /**
     * 클라이언트(Web/App)가 카카오/네이버 SDK로 발급받아 넘겨준 provider access token으로
     * 해당 provider의 사용자 정보 API를 호출해 신원을 확인한다.
     */
    SocialUserInfo fetchUserInfo(String accessToken);
}
