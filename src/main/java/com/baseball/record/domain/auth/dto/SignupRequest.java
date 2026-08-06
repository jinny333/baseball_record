package com.baseball.record.domain.auth.dto;

import com.baseball.record.domain.member.Provider;

public record SignupRequest(Provider provider, String accessToken, String nickname, String favoriteTeam,
                             String birth, String phoneNumber) {
}
