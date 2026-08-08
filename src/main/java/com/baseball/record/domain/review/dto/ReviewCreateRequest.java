package com.baseball.record.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReviewCreateRequest(
        @NotNull(message = "다이어리 ID는 필수입니다.")
        @Positive(message = "다이어리 ID는 양수여야 합니다.")
        Long recordId,

        @Size(max = 500, message = "시야 사진 URL은 500자 이하여야 합니다.")
        String viewImage,

        @NotBlank(message = "리뷰 내용은 필수입니다.")
        String reviewContent,

        List<@Pattern(regexp = "[^,]*", message = "키워드에는 콤마(,)를 포함할 수 없습니다.") String> keywords
) {
}
