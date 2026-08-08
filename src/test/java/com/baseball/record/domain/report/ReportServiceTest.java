package com.baseball.record.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.member.Provider;
import com.baseball.record.domain.report.dto.ReportCreateRequest;
import com.baseball.record.domain.review.ReviewRepository;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void 신고를_접수하면_PENDING_상태로_생성된다() {
        Member reporter = createMember(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
        given(reviewRepository.existsById(100L)).willReturn(true);

        var response = reportService.createReport(1L,
                new ReportCreateRequest(ReportTargetType.REVIEW, 100L, "부적절한 사진"));

        assertThat(response.reporterId()).isEqualTo(1L);
        assertThat(response.targetType()).isEqualTo(ReportTargetType.REVIEW);
        assertThat(response.status()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void 존재하지_않는_신고자면_예외가_발생한다() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.createReport(999L,
                new ReportCreateRequest(ReportTargetType.USER, 1L, "욕설")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_리뷰를_신고하면_예외가_발생한다() {
        Member reporter = createMember(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(reporter));
        given(reviewRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> reportService.createReport(1L,
                new ReportCreateRequest(ReportTargetType.REVIEW, 999L, "부적절한 사진")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REPORT_TARGET_NOT_FOUND);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void 신고를_처리하면_RESOLVED로_바뀐다() {
        Report report = createReport(createMember(1L), ReportTargetType.USER, 2L);
        given(reportRepository.findById(10L)).willReturn(Optional.of(report));

        var response = reportService.resolveReport(10L);

        assertThat(response.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 존재하지_않는_신고를_처리하면_예외가_발생한다() {
        given(reportRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.resolveReport(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REPORT_NOT_FOUND);
    }

    @Test
    void 상태_필터가_있으면_findByStatus로_조회한다() {
        given(reportRepository.findByStatus(ReportStatus.PENDING, Pageable.unpaged()))
                .willReturn(org.springframework.data.domain.Page.empty());

        reportService.getReports(ReportStatus.PENDING, Pageable.unpaged());

        verify(reportRepository).findByStatus(ReportStatus.PENDING, Pageable.unpaged());
        verify(reportRepository, never()).findAll(any(Pageable.class));
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

    private Report createReport(Member reporter, ReportTargetType targetType, Long targetId) {
        return Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reason("사유")
                .build();
    }
}
