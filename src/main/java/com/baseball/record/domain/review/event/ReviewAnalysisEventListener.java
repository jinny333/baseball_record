package com.baseball.record.domain.review.event;

import com.baseball.record.domain.review.Review;
import com.baseball.record.domain.review.ReviewRepository;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 리뷰 저장 트랜잭션과 형태소 분석/추천 벡터 계산 책임을 분리하는 지점.
 * AFTER_COMMIT에서만 처리해 아직 커밋되지 않은 리뷰를 읽는 경쟁 상태를 막고,
 * @Async(AsyncConfig의 전용 스레드풀)로 요청 스레드와 분리해 API 응답 지연을 없앤다.
 */
@Component
public class ReviewAnalysisEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewAnalysisEventListener.class);

    private final ReviewRepository reviewRepository;
    private final Komoran komoran;

    public ReviewAnalysisEventListener(ReviewRepository reviewRepository, Komoran komoran) {
        this.reviewRepository = reviewRepository;
        this.komoran = komoran;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewCreatedEvent event) {
        reviewRepository.findById(event.reviewId()).ifPresentOrElse(
                this::analyzeAndStoreVector,
                () -> log.warn("분석 대상 리뷰를 찾을 수 없습니다. reviewId={}", event.reviewId()));
    }

    private void analyzeAndStoreVector(Review review) {
        String source = review.getReviewContent() + " " + String.join(" ", review.getKeywords());
        KomoranResult result = komoran.analyze(source);

        Map<String, Long> vector = result.getNouns().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        review.updateKeywordVector(vector);
        reviewRepository.save(review);
    }
}
