package com.baseball.record.domain.review.event;

/**
 * 리뷰가 커밋된 이후, 형태소 분석/추천 벡터 계산을 트리거하기 위한 이벤트.
 * 요청 스레드는 리뷰 저장까지만 책임지고, 무거운 후처리는 리스너가 비동기로 넘겨받는다.
 */
public record ReviewCreatedEvent(Long reviewId) {
}
