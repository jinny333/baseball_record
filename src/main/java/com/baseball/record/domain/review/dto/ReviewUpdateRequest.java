package com.baseball.record.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ReviewUpdateRequest(
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        String reviewContent,

        List<String> keywords
) {
}
