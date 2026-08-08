package com.baseball.record.domain.auth.dto;

import com.baseball.record.domain.member.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotNull(message = "소셜 로그인 provider는 필수입니다.")
        Provider provider,

        @NotBlank(message = "소셜 액세스 토큰은 필수입니다.")
        String accessToken,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
        String nickname,

        @Size(max = 30, message = "응원 구단명은 30자 이하여야 합니다.")
        String favoriteTeam,

        @Size(max = 20, message = "생년월일은 20자 이하여야 합니다.")
        String birth,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        String phoneNumber
) {
}
