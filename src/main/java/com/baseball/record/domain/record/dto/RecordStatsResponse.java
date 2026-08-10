package com.baseball.record.domain.record.dto;

import java.util.List;

public record RecordStatsResponse(
        int totalWin,
        int totalLose,
        int totalDraw,
        double totalWinRate,
        List<TeamStatResponse> byOpponentTeam,
        List<StadiumStatResponse> byStadium
) {
}
