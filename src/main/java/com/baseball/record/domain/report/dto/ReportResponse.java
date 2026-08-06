package com.baseball.record.domain.report.dto;

import com.baseball.record.domain.report.Report;
import com.baseball.record.domain.report.ReportStatus;
import com.baseball.record.domain.report.ReportTargetType;
import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt());
    }
}
