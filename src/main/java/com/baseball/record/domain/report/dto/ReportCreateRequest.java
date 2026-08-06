package com.baseball.record.domain.report.dto;

import com.baseball.record.domain.report.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @NotNull(message = "신고 대상 유형은 필수입니다.")
        ReportTargetType targetType,

        @NotNull(message = "신고 대상 ID는 필수입니다.")
        Long targetId,

        @NotBlank(message = "신고 사유는 필수입니다.")
        String reason
) {
}
