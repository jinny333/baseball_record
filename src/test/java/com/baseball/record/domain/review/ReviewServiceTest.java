package com.baseball.record.domain.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.Provider;
import com.baseball.record.domain.record.MatchResult;
import com.baseball.record.domain.record.Record;
import com.baseball.record.domain.record.RecordRepository;
import com.baseball.record.domain.review.dto.ReviewCreateRequest;
import com.baseball.record.domain.review.dto.ReviewUpdateRequest;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void 본인_다이어리에_리뷰를_작성한다() {
        Record record = createRecord(createMember(1L));
        given(recordRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(record));
        given(reviewRepository.existsByRecord_Id(10L)).willReturn(false);

        var response = reviewService.createReview(1L, createRequest());

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.stadium()).isEqualTo("잠실");
        assertThat(response.keywords()).containsExactly("응원석", "통로");
        assertThat(response.status()).isEqualTo(ReviewStatus.ACTIVE);
    }

    @Test
    void 남의_다이어리에_리뷰를_작성하면_예외가_발생한다() {
        given(recordRepository.findByIdAndMember_Id(10L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(2L, createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RECORD_NOT_FOUND);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void 이미_리뷰가_있는_다이어리면_예외가_발생한다() {
        Record record = createRecord(createMember(1L));
        given(recordRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(record));
        given(reviewRepository.existsByRecord_Id(10L)).willReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(1L, createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void 리뷰를_상세_조회한다() {
        Review review = createReview(createRecord(createMember(1L)));
        given(reviewRepository.findById(100L)).willReturn(Optional.of(review));

        var response = reviewService.getReview(100L);

        assertThat(response.reviewContent()).isEqualTo("시야 좋아요");
    }

    @Test
    void 존재하지_않는_리뷰를_조회하면_예외가_발생한다() {
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReview(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    void 블라인드된_리뷰를_상세_조회하면_예외가_발생한다() {
        Review review = createReview(createRecord(createMember(1L)));
        review.blind();
        given(reviewRepository.findById(100L)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.getReview(100L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    void 본인_리뷰를_수정하면_내용과_키워드가_바뀐다() {
        Review review = createReview(createRecord(createMember(1L)));
        given(reviewRepository.findByIdAndMember_Id(100L, 1L)).willReturn(Optional.of(review));

        var response = reviewService.updateReview(1L, 100L,
                new ReviewUpdateRequest("생각보다 별로예요", List.of("햇빛")));

        assertThat(response.reviewContent()).isEqualTo("생각보다 별로예요");
        assertThat(response.keywords()).containsExactly("햇빛");
    }

    @Test
    void 남의_리뷰를_수정하면_예외가_발생한다() {
        given(reviewRepository.findByIdAndMember_Id(100L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(2L, 100L,
                new ReviewUpdateRequest("수정", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    void 본인_리뷰를_삭제한다() {
        Review review = createReview(createRecord(createMember(1L)));
        given(reviewRepository.findByIdAndMember_Id(100L, 1L)).willReturn(Optional.of(review));

        reviewService.deleteReview(1L, 100L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void 남의_리뷰를_삭제하면_예외가_발생한다() {
        given(reviewRepository.findByIdAndMember_Id(100L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(2L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        verify(reviewRepository, never()).delete(any());
    }

    private Member createMember(Long id) {
        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .socialId("social-" + id)
                .email("user" + id + "@test.com")
                .nickname("nick" + id)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Record createRecord(Member member) {
        Record record = Record.builder()
                .member(member)
                .matchDate(LocalDate.of(2026, 8, 7))
                .stadium("잠실")
                .homeTeam("LG")
                .awayTeam("두산")
                .myTeam("LG")
                .matchResult(MatchResult.WIN)
                .section("1루내야석A")
                .block("207")
                .rowNum("5")
                .seatNum("12")
                .build();
        ReflectionTestUtils.setField(record, "id", 10L);
        return record;
    }

    private Review createReview(Record record) {
        Review review = Review.builder()
                .record(record)
                .member(record.getMember())
                .reviewContent("시야 좋아요")
                .keywords(List.of("응원석"))
                .stadium(record.getStadium())
                .section(record.getSection())
                .block(record.getBlock())
                .rowNum(record.getRowNum())
                .seatNum(record.getSeatNum())
                .build();
        ReflectionTestUtils.setField(review, "id", 100L);
        return review;
    }

    private ReviewCreateRequest createRequest() {
        return new ReviewCreateRequest(10L, "http://image/view.png", "시야 좋아요", List.of("응원석", "통로"));
    }
}
