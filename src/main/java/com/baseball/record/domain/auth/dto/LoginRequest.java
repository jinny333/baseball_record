package com.baseball.record.domain.auth.dto;

import com.baseball.record.domain.member.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "소셜 로그인 provider는 필수입니다.")
        Provider provider,

        @NotBlank(message = "소셜 액세스 토큰은 필수입니다.")
        String accessToken
) {
}
