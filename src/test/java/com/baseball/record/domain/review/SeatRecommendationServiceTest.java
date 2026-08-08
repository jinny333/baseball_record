package com.baseball.record.domain.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.baseball.record.domain.review.dto.SeatRecommendationResponse;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SeatRecommendationServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private Komoran komoran;

    @InjectMocks
    private SeatRecommendationService seatRecommendationService;

    @Test
    void 키워드와_겹치는_리뷰가_있는_좌석_블록을_점수순으로_추천한다() {
        KomoranResult queryResult = mock(KomoranResult.class);
        given(queryResult.getNouns()).willReturn(List.of("응원가"));
        given(komoran.analyze(anyString())).willReturn(queryResult);

        Review wellMatched = reviewWithVector(1L, "잠실", "1루내야석A", "207", Map.of("응원가", 3L, "통로", 1L));
        Review poorlyMatched = reviewWithVector(2L, "잠실", "1루내야석A", "207", Map.of("응원가", 1L, "햇빛", 5L));
        Review notMatched = reviewWithVector(3L, "잠실", "3루외야석", "101", Map.of("햇빛", 2L));

        given(reviewRepository.findAllByStatus(ReviewStatus.ACTIVE))
                .willReturn(List.of(wellMatched, poorlyMatched, notMatched));

        var responses = seatRecommendationService.recommend(null, List.of("응원가 부르기 좋은"), null);

        assertThat(responses).hasSize(1);
        SeatRecommendationResponse top = responses.get(0);
        assertThat(top.block()).isEqualTo("207");
        assertThat(top.matchedReviewCount()).isEqualTo(2);
        assertThat(top.score()).isGreaterThan(0);
        assertThat(top.sampleReviewIds()).contains(1L, 2L);
    }

    @Test
    void 키워드가_없으면_레포지토리_조회_없이_빈_목록을_반환한다() {
        var responses = seatRecommendationService.recommend(null, List.of(), null);

        assertThat(responses).isEmpty();
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void limit이_0이하이면_예외가_발생한다() {
        assertThatThrownBy(() -> seatRecommendationService.recommend(null, List.of("응원가"), -1))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_LIMIT);
        verifyNoInteractions(reviewRepository);
        verifyNoInteractions(komoran);
    }

    private Review reviewWithVector(Long id, String stadium, String section, String block, Map<String, Long> vector) {
        Review review = Review.builder()
                .reviewContent("내용")
                .keywords(List.of())
                .stadium(stadium)
                .section(section)
                .block(block)
                .build();
        ReflectionTestUtils.setField(review, "id", id);
        review.updateKeywordVector(vector);
        return review;
    }
}
