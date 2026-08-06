package com.baseball.record.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
        String nickname,

        @Size(max = 30, message = "응원 구단명은 30자 이하여야 합니다.")
        String favoriteTeam
) {
}
