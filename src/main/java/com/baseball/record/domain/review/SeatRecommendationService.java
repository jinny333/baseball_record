package com.baseball.record.domain.review;

import com.baseball.record.domain.review.dto.SeatRecommendationResponse;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "응원가 부르기 좋은", "햇빛이 안 드는" 같은 사용자 선택 키워드를 형태소 분석해,
 * ReviewAnalysisEventListener가 리뷰 작성 시 비동기로 채워둔 Review.keywordVector와 코사인 유사도로 비교한다.
 * 리뷰 단위가 아니라 같은 좌석 블록(stadium/section/block)으로 묶어 평균 점수 높은 순으로 추천한다.
 */
@Service
@Transactional(readOnly = true)
public class SeatRecommendationService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int SAMPLE_REVIEW_LIMIT = 3;

    private final ReviewRepository reviewRepository;
    private final Komoran komoran;

    public SeatRecommendationService(ReviewRepository reviewRepository, Komoran komoran) {
        this.reviewRepository = reviewRepository;
        this.komoran = komoran;
    }

    public List<SeatRecommendationResponse> recommend(String stadium, List<String> keywords, Integer limit) {
        Map<String, Long> queryVector = analyzeQueryVector(keywords);
        if (queryVector.isEmpty()) {
            return List.of();
        }

        List<Review> candidates = stadium != null
                ? reviewRepository.findAllByStadiumAndStatus(stadium, ReviewStatus.ACTIVE)
                : reviewRepository.findAllByStatus(ReviewStatus.ACTIVE);

        Map<SeatBlockKey, List<ScoredReview>> grouped = candidates.stream()
                .map(review -> new ScoredReview(review, cosineSimilarity(queryVector, review.getKeywordVector())))
                .filter(scored -> scored.score() > 0)
                .collect(Collectors.groupingBy(scored -> SeatBlockKey.from(scored.review())));

        return grouped.values().stream()
                .map(this::toResponse)
                .sorted(Comparator.comparingDouble(SeatRecommendationResponse::score).reversed())
                .limit(limit == null ? DEFAULT_LIMIT : limit)
                .toList();
    }

    private Map<String, Long> analyzeQueryVector(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Map.of();
        }
        KomoranResult result = komoran.analyze(String.join(" ", keywords));
        return result.getNouns().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    // 고정 차원 벡터를 쓰지 않고, 두 리뷰가 공유하는 term만으로 내적을 구하는 희소 벡터 코사인 유사도.
    private double cosineSimilarity(Map<String, Long> a, Map<String, Long> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> sharedTerms = new HashSet<>(a.keySet());
        sharedTerms.retainAll(b.keySet());

        double dotProduct = sharedTerms.stream()
                .mapToDouble(term -> a.get(term) * b.get(term))
                .sum();
        double normA = Math.sqrt(a.values().stream().mapToDouble(v -> v * v).sum());
        double normB = Math.sqrt(b.values().stream().mapToDouble(v -> v * v).sum());
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (normA * normB);
    }

    private SeatRecommendationResponse toResponse(List<ScoredReview> scoredReviews) {
        SeatBlockKey key = SeatBlockKey.from(scoredReviews.get(0).review());
        double averageScore = scoredReviews.stream().mapToDouble(ScoredReview::score).average().orElse(0.0);
        List<Long> sampleReviewIds = scoredReviews.stream()
                .sorted(Comparator.comparingDouble(ScoredReview::score).reversed())
                .limit(SAMPLE_REVIEW_LIMIT)
                .map(scored -> scored.review().getId())
                .toList();

        return new SeatRecommendationResponse(
                key.stadium(), key.section(), key.block(), averageScore, scoredReviews.size(), sampleReviewIds);
    }

    private record ScoredReview(Review review, double score) {
    }

    private record SeatBlockKey(String stadium, String section, String block) {
        static SeatBlockKey from(Review review) {
            return new SeatBlockKey(review.getStadium(), review.getSection(), review.getBlock());
        }
    }
}
