package com.baseball.record.domain.record.dto;

public record TeamStatResponse(
        String opponentTeam,
        int win,
        int lose,
        int draw,
        double winRate
) {
}
