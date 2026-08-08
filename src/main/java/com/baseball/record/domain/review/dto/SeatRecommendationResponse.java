package com.baseball.record.domain.review.dto;

import java.util.List;

public record SeatRecommendationResponse(
        String stadium,
        String section,
        String block,
        double score,
        long matchedReviewCount,
        List<Long> sampleReviewIds
) {
}
