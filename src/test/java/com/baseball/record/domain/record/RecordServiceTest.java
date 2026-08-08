package com.baseball.record.domain.record;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.member.Provider;
import com.baseball.record.domain.record.dto.RecordCreateRequest;
import com.baseball.record.domain.record.dto.RecordUpdateRequest;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RecordService recordService;

    @Test
    void 다이어리를_작성한다() {
        Member member = createMember(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        var response = recordService.createRecord(1L, createRequest());

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.stadium()).isEqualTo("잠실");
        assertThat(response.matchResult()).isEqualTo(MatchResult.WIN);
    }

    @Test
    void 존재하지_않는_회원이_작성하면_예외가_발생한다() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.createRecord(999L, createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void 본인_다이어리를_조회한다() {
        Record record = createRecord(createMember(1L));
        given(recordRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(record));

        var response = recordService.getRecord(1L, 10L);

        assertThat(response.stadium()).isEqualTo("잠실");
    }

    @Test
    void 남의_다이어리를_조회하면_404_예외가_발생한다() {
        given(recordRepository.findByIdAndMember_Id(10L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecord(2L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RECORD_NOT_FOUND);
    }

    @Test
    void 다이어리를_수정하면_결과_코멘트_이미지만_바뀐다() {
        Record record = createRecord(createMember(1L));
        given(recordRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(record));

        var response = recordService.updateRecord(1L, 10L,
                new RecordUpdateRequest(MatchResult.LOSE, "아쉬운 경기", "http://image/after.png"));

        assertThat(response.matchResult()).isEqualTo(MatchResult.LOSE);
        assertThat(response.diaryComment()).isEqualTo("아쉬운 경기");
        assertThat(response.stadium()).isEqualTo("잠실");
    }

    @Test
    void 남의_다이어리를_수정하면_404_예외가_발생한다() {
        given(recordRepository.findByIdAndMember_Id(10L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.updateRecord(2L, 10L,
                new RecordUpdateRequest(MatchResult.LOSE, "코멘트", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RECORD_NOT_FOUND);
    }

    @Test
    void 본인_다이어리를_삭제한다() {
        Record record = createRecord(createMember(1L));
        given(recordRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(record));

        recordService.deleteRecord(1L, 10L);

        verify(recordRepository).delete(record);
    }

    @Test
    void 남의_다이어리를_삭제하면_404_예외가_발생한다() {
        given(recordRepository.findByIdAndMember_Id(10L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.deleteRecord(2L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RECORD_NOT_FOUND);
        verify(recordRepository, never()).delete(any());
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
        return Record.builder()
                .member(member)
                .matchDate(LocalDate.of(2026, 8, 7))
                .stadium("잠실")
                .homeTeam("LG")
                .awayTeam("두산")
                .myTeam("LG")
                .matchResult(MatchResult.WIN)
                .diaryComment("즐거운 직관")
                .build();
    }

    private RecordCreateRequest createRequest() {
        return new RecordCreateRequest(
                LocalDate.of(2026, 8, 7), "잠실", "LG", "두산", "LG", MatchResult.WIN,
                "1루내야석A", "207", "5", "12", "즐거운 직관", "http://image/ticket.png");
    }
}
