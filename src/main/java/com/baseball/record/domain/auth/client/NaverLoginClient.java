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
public class NaverLoginClient implements SocialLoginClient {

    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";

    private final RestClient restClient = RestClient.create();

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.NAVER;
    }

    @Override
    public SocialUserInfo fetchUserInfo(String accessToken) {
        try {
            NaverUserResponse response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(NaverUserResponse.class);

            NaverUserResponse.NaverAccount account = response != null ? response.response() : null;
            if (account == null || account.id() == null) {
                throw new BusinessException(ErrorCode.INVALID_PROVIDER_TOKEN);
            }

            return new SocialUserInfo(account.id(), account.email());
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INVALID_PROVIDER_TOKEN);
        }
    }
}
