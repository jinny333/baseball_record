package com.baseball.record.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record ReviewUpdateRequest(
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        String reviewContent,

        List<@Pattern(regexp = "[^,]*", message = "키워드에는 콤마(,)를 포함할 수 없습니다.") String> keywords
) {
}
