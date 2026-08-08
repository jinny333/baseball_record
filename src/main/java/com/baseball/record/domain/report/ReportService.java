package com.baseball.record.domain.report;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.report.dto.ReportCreateRequest;
import com.baseball.record.domain.report.dto.ReportResponse;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    public ReportService(ReportRepository reportRepository, MemberRepository memberRepository) {
        this.reportRepository = reportRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReportResponse createReport(Long reporterId, ReportCreateRequest request) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        validateTargetExists(request.targetType(), request.targetId());

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .build();
        reportRepository.save(report);

        return ReportResponse.from(report);
    }

    public Page<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
        Page<Report> reports = status != null
                ? reportRepository.findByStatus(status, pageable)
                : reportRepository.findAll(pageable);
        return reports.map(ReportResponse::from);
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
        report.resolve();
        return ReportResponse.from(report);
    }

    // REVIEW 도메인이 아직 없어 REVIEW 타입은 존재 검증 없이 통과시킨다. REVIEW 도메인 추가 시 여기도 함께 검증할 것.
    private void validateTargetExists(ReportTargetType targetType, Long targetId) {
        if (targetType == ReportTargetType.USER && !memberRepository.existsById(targetId)) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }
}
