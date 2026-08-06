package com.baseball.record.domain.auth.client;

import com.baseball.record.domain.auth.SocialLoginClient;
import com.baseball.record.domain.auth.SocialUserInfo;
import com.baseball.record.domain.member.Provider;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoLoginClient implements SocialLoginClient {

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient = RestClient.create();

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.KAKAO;
    }

    @Override
    public SocialUserInfo fetchUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (response == null || response.id() == null) {
                throw new BusinessException(ErrorCode.INVALID_PROVIDER_TOKEN);
            }

            String email = response.kakaoAccount() != null ? response.kakaoAccount().email() : null;
            return new SocialUserInfo(String.valueOf(response.id()), email);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }
    }
}
