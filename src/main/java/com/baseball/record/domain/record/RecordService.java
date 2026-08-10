package com.baseball.record.domain.record;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.record.dto.RecordCreateRequest;
import com.baseball.record.domain.record.dto.RecordResponse;
import com.baseball.record.domain.record.dto.RecordStatsResponse;
import com.baseball.record.domain.record.dto.RecordUpdateRequest;
import com.baseball.record.domain.record.dto.StadiumStatResponse;
import com.baseball.record.domain.record.dto.TeamStatResponse;
import com.baseball.record.domain.review.ReviewRepository;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecordService {

    private final RecordRepository recordRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    public RecordService(RecordRepository recordRepository, MemberRepository memberRepository,
                          ReviewRepository reviewRepository) {
        this.recordRepository = recordRepository;
        this.memberRepository = memberRepository;
        this.reviewRepository = reviewRepository;
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

    // 취소 경기를 뺀 나머지를 상대팀별/구장별로 묶어 승/패/무 카운트와 승률(%)을 계산한다.
    // SeatRecommendationService와 동일하게, 레포지토리는 평탄한 목록만 반환하고
    // 그룹핑·집계는 서비스 계층에서 스트림으로 처리한다.
    public RecordStatsResponse getStats(Long memberId) {
        List<Record> records = recordRepository.findAllByMember_IdAndMatchResultNot(memberId, MatchResult.CANCEL);

        List<TeamStatResponse> byOpponentTeam = records.stream()
                .collect(Collectors.groupingBy(this::opponentTeam))
                .entrySet().stream()
                .map(entry -> toTeamStat(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt((TeamStatResponse stat) -> stat.win() + stat.lose() + stat.draw())
                        .reversed())
                .toList();

        List<StadiumStatResponse> byStadium = records.stream()
                .collect(Collectors.groupingBy(Record::getStadium))
                .entrySet().stream()
                .map(entry -> toStadiumStat(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt((StadiumStatResponse stat) -> stat.win() + stat.lose() + stat.draw())
                        .reversed())
                .toList();

        Map<MatchResult, Long> totalCounts = countByResult(records);
        long totalWin = totalCounts.getOrDefault(MatchResult.WIN, 0L);
        long totalLose = totalCounts.getOrDefault(MatchResult.LOSE, 0L);
        long totalDraw = totalCounts.getOrDefault(MatchResult.DRAW, 0L);

        return new RecordStatsResponse(
                (int) totalWin, (int) totalLose, (int) totalDraw, winRate(totalWin, totalLose),
                byOpponentTeam, byStadium);
    }

    @Transactional
    public RecordResponse updateRecord(Long memberId, Long recordId, RecordUpdateRequest request) {
        Record record = getOwnedRecord(memberId, recordId);
        record.update(request.matchResult(), request.diaryComment(), request.imageUrl());
        return RecordResponse.from(record);
    }

    @Transactional
    public void deleteRecord(Long memberId, Long recordId) {
        Record record = getOwnedRecord(memberId, recordId);
        if (reviewRepository.existsByRecord_Id(record.getId())) {
            throw new BusinessException(ErrorCode.RECORD_HAS_REVIEW);
        }
        recordRepository.delete(record);
    }

    // 소유권 검증을 쿼리 레벨에서 한 번에 처리해, "존재하지 않음"과 "남의 글"을 모두 RECORD_NOT_FOUND(404)로 통일한다.
    // 둘을 구분해 응답하면(403 vs 404) 다이어리 ID를 순차 스캔해 존재 여부를 알아내는 사이드채널이 열린다.
    private Record getOwnedRecord(Long memberId, Long recordId) {
        return recordRepository.findByIdAndMember_Id(recordId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));
    }

    // 홈/원정 중 내 응원팀이 아닌 쪽을 상대팀으로 본다.
    private String opponentTeam(Record record) {
        return record.getMyTeam().equals(record.getHomeTeam()) ? record.getAwayTeam() : record.getHomeTeam();
    }

    private TeamStatResponse toTeamStat(String opponentTeam, List<Record> records) {
        Map<MatchResult, Long> counts = countByResult(records);
        long win = counts.getOrDefault(MatchResult.WIN, 0L);
        long lose = counts.getOrDefault(MatchResult.LOSE, 0L);
        long draw = counts.getOrDefault(MatchResult.DRAW, 0L);
        return new TeamStatResponse(opponentTeam, (int) win, (int) lose, (int) draw, winRate(win, lose));
    }

    private StadiumStatResponse toStadiumStat(String stadium, List<Record> records) {
        Map<MatchResult, Long> counts = countByResult(records);
        long win = counts.getOrDefault(MatchResult.WIN, 0L);
        long lose = counts.getOrDefault(MatchResult.LOSE, 0L);
        long draw = counts.getOrDefault(MatchResult.DRAW, 0L);
        return new StadiumStatResponse(stadium, (int) win, (int) lose, (int) draw, winRate(win, lose));
    }

    private Map<MatchResult, Long> countByResult(List<Record> records) {
        return records.stream().collect(Collectors.groupingBy(Record::getMatchResult, Collectors.counting()));
    }

    // 무승부는 분모에서 제외하는 통상적인 야구 승률 계산식(승 / (승+패)). 소수점 첫째 자리까지 반올림한 퍼센트.
    private double winRate(long win, long lose) {
        long decisiveGames = win + lose;
        if (decisiveGames == 0) {
            return 0.0;
        }
        return Math.round((double) win / decisiveGames * 1000) / 10.0;
    }
}
