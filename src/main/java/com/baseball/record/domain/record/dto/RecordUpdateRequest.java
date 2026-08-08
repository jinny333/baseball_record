package com.baseball.record.domain.record.dto;

import com.baseball.record.domain.record.MatchResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 직관 사실(날짜/구장/좌석)은 불변, 사후 기록(결과 확정·소감·사진)만 수정 가능하다는
 * Record.update()의 기존 설계를 그대로 따른다.
 */
public record RecordUpdateRequest(
        @NotNull(message = "경기 결과는 필수입니다.")
        MatchResult matchResult,

        String diaryComment,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String imageUrl
) {
}
