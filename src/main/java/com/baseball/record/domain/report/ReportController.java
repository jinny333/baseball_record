package com.baseball.record.domain.report;

import com.baseball.record.domain.report.dto.ReportCreateRequest;
import com.baseball.record.domain.report.dto.ReportResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/v1/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse createReport(@AuthenticationPrincipal Long memberId,
                                        @Valid @RequestBody ReportCreateRequest request) {
        return reportService.createReport(memberId, request);
    }

    @GetMapping("/api/v1/admin/reports")
    public Page<ReportResponse> getReports(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reportService.getReports(status, pageable);
    }

    @PatchMapping("/api/v1/admin/reports/{reportId}")
    public ReportResponse resolveReport(@PathVariable Long reportId) {
        return reportService.resolveReport(reportId);
    }
}
