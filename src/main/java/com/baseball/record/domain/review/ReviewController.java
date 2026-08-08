package com.baseball.record.domain.review;

import com.baseball.record.domain.review.dto.ReviewCreateRequest;
import com.baseball.record.domain.review.dto.ReviewResponse;
import com.baseball.record.domain.review.dto.ReviewUpdateRequest;
import com.baseball.record.domain.review.dto.SeatRecommendationResponse;
import com.baseball.record.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    private final ReviewService reviewService;
    private final SeatRecommendationService seatRecommendationService;

    public ReviewController(ReviewService reviewService, SeatRecommendationService seatRecommendationService) {
        this.reviewService = reviewService;
        this.seatRecommendationService = seatRecommendationService;
    }

    // /reviews/{reviewId}(변수 경로)보다 먼저 매칭되도록 Spring이 리터럴 경로를 더 구체적인 패턴으로 우선 처리한다.
    @GetMapping("/api/v1/reviews/recommend")
    public List<SeatRecommendationResponse> recommendSeats(
            @RequestParam(required = false) String stadium,
            @RequestParam List<String> keywords,
            @RequestParam(required = false) Integer limit) {
        return seatRecommendationService.recommend(stadium, keywords, limit);
    }

    @PostMapping("/api/v1/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@CurrentMemberId Long memberId,
                                        @Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.createReview(memberId, request);
    }

    @GetMapping("/api/v1/reviews")
    public Page<ReviewResponse> getReviews(
            @RequestParam(required = false) String stadium,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String block,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.searchReviews(stadium, section, block, pageable);
    }

    @GetMapping("/api/v1/reviews/{reviewId}")
    public ReviewResponse getReview(@PathVariable Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    @PatchMapping("/api/v1/reviews/{reviewId}")
    public ReviewResponse updateReview(@CurrentMemberId Long memberId,
                                        @PathVariable Long reviewId,
                                        @Valid @RequestBody ReviewUpdateRequest request) {
        return reviewService.updateReview(memberId, reviewId, request);
    }

    @DeleteMapping("/api/v1/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@CurrentMemberId Long memberId, @PathVariable Long reviewId) {
        reviewService.deleteReview(memberId, reviewId);
    }
}
