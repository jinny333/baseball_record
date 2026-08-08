package com.baseball.record.domain.record.dto;

import com.baseball.record.domain.record.MatchResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RecordCreateRequest(
        @NotNull(message = "경기 날짜는 필수입니다.")
        LocalDate matchDate,

        @NotBlank(message = "구장명은 필수입니다.")
        @Size(max = 30, message = "구장명은 30자 이하여야 합니다.")
        String stadium,

        @NotBlank(message = "홈 팀은 필수입니다.")
        @Size(max = 30, message = "홈 팀은 30자 이하여야 합니다.")
        String homeTeam,

        @NotBlank(message = "원정 팀은 필수입니다.")
        @Size(max = 30, message = "원정 팀은 30자 이하여야 합니다.")
        String awayTeam,

        @NotBlank(message = "응원 팀은 필수입니다.")
        @Size(max = 30, message = "응원 팀은 30자 이하여야 합니다.")
        String myTeam,

        @NotNull(message = "경기 결과는 필수입니다.")
        MatchResult matchResult,

        @Size(max = 30, message = "구역은 30자 이하여야 합니다.")
        String section,

        @Size(max = 30, message = "블록은 30자 이하여야 합니다.")
        String block,

        @Size(max = 10, message = "열은 10자 이하여야 합니다.")
        String rowNum,

        @Size(max = 10, message = "좌석 번호는 10자 이하여야 합니다.")
        String seatNum,

        String diaryComment,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String imageUrl
) {
}
