package com.baseball.record.domain.record.dto;

import com.baseball.record.domain.record.MatchResult;
import com.baseball.record.domain.record.Record;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecordResponse(
        Long id,
        Long memberId,
        LocalDate matchDate,
        String stadium,
        String homeTeam,
        String awayTeam,
        String myTeam,
        MatchResult matchResult,
        String section,
        String block,
        String rowNum,
        String seatNum,
        String diaryComment,
        String imageUrl,
        LocalDateTime createdAt
) {

    public static RecordResponse from(Record record) {
        return new RecordResponse(
                record.getId(),
                record.getMember().getId(),
                record.getMatchDate(),
                record.getStadium(),
                record.getHomeTeam(),
                record.getAwayTeam(),
                record.getMyTeam(),
                record.getMatchResult(),
                record.getSection(),
                record.getBlock(),
                record.getRowNum(),
                record.getSeatNum(),
                record.getDiaryComment(),
                record.getImageUrl(),
                record.getCreatedAt());
    }
}
