package com.baseball.record.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.baseball.record.domain.auth.RefreshTokenRepository;
import com.baseball.record.domain.member.dto.MemberUpdateRequest;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 존재하는_회원의_프로필을_조회한다() {
        Member member = createMember(1L, "치즈볼", "LG");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        var response = memberService.getMyProfile(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("치즈볼");
        assertThat(response.favoriteTeam()).isEqualTo("LG");
    }

    @Test
    void 존재하지_않는_회원을_조회하면_예외가_발생한다() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMyProfile(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    void 프로필을_수정하면_닉네임과_응원구단이_변경된다() {
        Member member = createMember(1L, "치즈볼", "LG");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        var response = memberService.updateProfile(1L, new MemberUpdateRequest("직관러버", "두산"));

        assertThat(response.nickname()).isEqualTo("직관러버");
        assertThat(response.favoriteTeam()).isEqualTo("두산");
    }

    @Test
    void 탈퇴하면_상태가_INACTIVE로_바뀌고_refresh_token이_전부_폐기된다() {
        Member member = createMember(1L, "치즈볼", "LG");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        memberService.withdraw(1L);

        assertThat(member.getStatus()).isEqualTo(MemberStatus.INACTIVE);
        verify(memberRepository).findById(1L);
        verify(refreshTokenRepository).deleteByMember(member);
    }

    @Test
    void 관리자가_회원_상태를_SUSPENDED로_변경하면_refresh_token이_전부_폐기된다() {
        Member member = createMember(1L, "치즈볼", "LG");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        var response = memberService.updateStatus(1L, MemberStatus.SUSPENDED);

        assertThat(response.status()).isEqualTo(MemberStatus.SUSPENDED);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
        verify(refreshTokenRepository).deleteByMember(member);
    }

    @Test
    void 관리자가_회원_상태를_ACTIVE로_복구하면_refresh_token을_건드리지_않는다() {
        Member member = createMember(1L, "치즈볼", "LG");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        memberService.updateStatus(1L, MemberStatus.ACTIVE);

        verify(refreshTokenRepository, never()).deleteByMember(member);
    }

    private Member createMember(Long id, String nickname, String favoriteTeam) {
        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .socialId("social-" + id)
                .email("user" + id + "@test.com")
                .nickname(nickname)
                .favoriteTeam(favoriteTeam)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
