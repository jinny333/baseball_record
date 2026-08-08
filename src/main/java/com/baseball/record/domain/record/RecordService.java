package com.baseball.record.domain.record;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.record.dto.RecordCreateRequest;
import com.baseball.record.domain.record.dto.RecordResponse;
import com.baseball.record.domain.record.dto.RecordUpdateRequest;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecordService {

    private final RecordRepository recordRepository;
    private final MemberRepository memberRepository;

    public RecordService(RecordRepository recordRepository, MemberRepository memberRepository) {
        this.recordRepository = recordRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public RecordResponse createRecord(Long memberId, RecordCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Record record = Record.builder()
                .member(member)
                .matchDate(request.matchDate())
                .stadium(request.stadium())
                .homeTeam(request.homeTeam())
                .awayTeam(request.awayTeam())
                .myTeam(request.myTeam())
                .matchResult(request.matchResult())
                .section(request.section())
                .block(request.block())
                .rowNum(request.rowNum())
                .seatNum(request.seatNum())
                .diaryComment(request.diaryComment())
                .imageUrl(request.imageUrl())
                .build();
        recordRepository.save(record);

        return RecordResponse.from(record);
    }

    public Page<RecordResponse> getRecords(Long memberId, Pageable pageable) {
        return recordRepository.findAllByMember_Id(memberId, pageable)
                .map(RecordResponse::from);
    }

    public RecordResponse getRecord(Long memberId, Long recordId) {
        return RecordResponse.from(getOwnedRecord(memberId, recordId));
    }

    @Transactional
    public RecordResponse updateRecord(Long memberId, Long recordId, RecordUpdateRequest request) {
        Record record = getOwnedRecord(memberId, recordId);
        record.update(request.matchResult(), request.diaryComment(), request.imageUrl());
        return RecordResponse.from(record);
    }

    @Transactional
    public void deleteRecord(Long memberId, Long recordId) {
        recordRepository.delete(getOwnedRecord(memberId, recordId));
    }

    // 소유권 검증을 쿼리 레벨에서 한 번에 처리해, "존재하지 않음"과 "남의 글"을 모두 RECORD_NOT_FOUND(404)로 통일한다.
    // 둘을 구분해 응답하면(403 vs 404) 다이어리 ID를 순차 스캔해 존재 여부를 알아내는 사이드채널이 열린다.
    private Record getOwnedRecord(Long memberId, Long recordId) {
        return recordRepository.findByIdAndMember_Id(recordId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));
    }
}
