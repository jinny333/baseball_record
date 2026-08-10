package com.baseball.record.domain.record.dto;

public record StadiumStatResponse(
        String stadium,
        int win,
        int lose,
        int draw,
        double winRate
) {
}
