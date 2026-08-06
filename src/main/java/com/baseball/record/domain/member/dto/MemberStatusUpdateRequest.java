package com.baseball.record.domain.member.dto;

import com.baseball.record.domain.member.MemberStatus;
import jakarta.validation.constraints.NotNull;

public record MemberStatusUpdateRequest(
        @NotNull(message = "변경할 계정 상태는 필수입니다.")
        MemberStatus status
) {
}
