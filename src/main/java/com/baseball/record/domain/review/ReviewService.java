package com.baseball.record.domain.review;

import com.baseball.record.domain.record.Record;
import com.baseball.record.domain.record.RecordRepository;
import com.baseball.record.domain.review.dto.ReviewCreateRequest;
import com.baseball.record.domain.review.dto.ReviewResponse;
import com.baseball.record.domain.review.dto.ReviewUpdateRequest;
import com.baseball.record.domain.review.event.ReviewCreatedEvent;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RecordRepository recordRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewService(ReviewRepository reviewRepository, RecordRepository recordRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.recordRepository = recordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReviewResponse createReview(Long memberId, ReviewCreateRequest request) {
        Record record = recordRepository.findByIdAndMember_Id(request.recordId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));
        if (reviewRepository.existsByRecord_Id(record.getId())) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .record(record)
                .member(record.getMember())
                .viewImage(request.viewImage())
                .reviewContent(request.reviewContent())
                .keywords(request.keywords())
                .stadium(record.getStadium())
                .section(record.getSection())
                .block(record.getBlock())
                .rowNum(record.getRowNum())
                .seatNum(record.getSeatNum())
                .build();
        reviewRepository.save(review);
        eventPublisher.publishEvent(new ReviewCreatedEvent(review.getId()));

        return ReviewResponse.from(review);
    }

    public Page<ReviewResponse> searchReviews(String stadium, String section, String block, Pageable pageable) {
        return reviewRepository.searchActive(stadium, section, block, pageable)
                .map(ReviewResponse::from);
    }

    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> r.getStatus() == ReviewStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request) {
        Review review = getOwnedReview(memberId, reviewId);
        review.update(request.reviewContent(), request.keywords());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        reviewRepository.delete(getOwnedReview(memberId, reviewId));
    }

    private Review getOwnedReview(Long memberId, Long reviewId) {
        return reviewRepository.findByIdAndMember_Id(reviewId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }
}
