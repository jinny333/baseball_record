package com.baseball.record.domain.record;

import com.baseball.record.domain.record.dto.OcrRequest;
import com.baseball.record.domain.record.dto.OcrResponse;
import com.baseball.record.domain.record.dto.RecordCreateRequest;
import com.baseball.record.domain.record.dto.RecordResponse;
import com.baseball.record.domain.record.dto.RecordStatsResponse;
import com.baseball.record.domain.record.dto.RecordUpdateRequest;
import com.baseball.record.domain.record.ocr.TicketOcrService;
import com.baseball.record.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecordController {

    private final RecordService recordService;
    private final TicketOcrService ticketOcrService;

    public RecordController(RecordService recordService, TicketOcrService ticketOcrService) {
        this.recordService = recordService;
        this.ticketOcrService = ticketOcrService;
    }

    @PostMapping("/api/v1/diaries/ocr")
    public OcrResponse parseTicketImage(@Valid @RequestBody OcrRequest request) {
        return ticketOcrService.parseTicket(request);
    }

    @PostMapping("/api/v1/records")
    @ResponseStatus(HttpStatus.CREATED)
    public RecordResponse createRecord(@CurrentMemberId Long memberId,
                                        @Valid @RequestBody RecordCreateRequest request) {
        return recordService.createRecord(memberId, request);
    }

    @GetMapping("/api/v1/records")
    public Page<RecordResponse> getRecords(
            @CurrentMemberId Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return recordService.getRecords(memberId, pageable);
    }

    // /records/{recordId}(변수 경로)보다 먼저 매칭되도록 Spring이 리터럴 경로를 더 구체적인 패턴으로 우선 처리한다.
    @GetMapping("/api/v1/records/stats")
    public RecordStatsResponse getStats(@CurrentMemberId Long memberId) {
        return recordService.getStats(memberId);
    }

    @GetMapping("/api/v1/records/{recordId}")
    public RecordResponse getRecord(@CurrentMemberId Long memberId, @PathVariable Long recordId) {
        return recordService.getRecord(memberId, recordId);
    }

    @PatchMapping("/api/v1/records/{recordId}")
    public RecordResponse updateRecord(@CurrentMemberId Long memberId,
                                        @PathVariable Long recordId,
                                        @Valid @RequestBody RecordUpdateRequest request) {
        return recordService.updateRecord(memberId, recordId, request);
    }

    @DeleteMapping("/api/v1/records/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(@CurrentMemberId Long memberId, @PathVariable Long recordId) {
        recordService.deleteRecord(memberId, recordId);
    }
}
