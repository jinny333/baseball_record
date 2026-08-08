package com.baseball.record.domain.review.dto;

import com.baseball.record.domain.review.Review;
import com.baseball.record.domain.review.ReviewStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long id,
        Long recordId,
        Long memberId,
        String viewImage,
        String reviewContent,
        List<String> keywords,
        ReviewStatus status,
        String stadium,
        String section,
        String block,
        String rowNum,
        String seatNum,
        LocalDateTime createdAt
) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRecord().getId(),
                review.getMember().getId(),
                review.getViewImage(),
                review.getReviewContent(),
                review.getKeywords(),
                review.getStatus(),
                review.getStadium(),
                review.getSection(),
                review.getBlock(),
                review.getRowNum(),
                review.getSeatNum(),
                review.getCreatedAt());
    }
}
