package com.baseball.record.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.baseball.record.domain.auth.dto.LoginRequest;
import com.baseball.record.domain.auth.dto.ReissueRequest;
import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.member.MemberStatus;
import com.baseball.record.domain.member.Provider;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import com.baseball.record.global.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SocialLoginClient kakaoLoginClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(List.of(kakaoLoginClient), memberRepository, refreshTokenRepository, jwtTokenProvider);
    }

    @Test
    void 정지된_회원은_로그인해도_토큰이_발급되지_않는다() {
        Member suspended = createMember(1L, MemberStatus.SUSPENDED);
        given(kakaoLoginClient.supports(Provider.KAKAO)).willReturn(true);
        given(kakaoLoginClient.fetchUserInfo("social-access-token"))
                .willReturn(new SocialUserInfo("social-1", "user1@test.com"));
        given(memberRepository.findByProviderAndSocialId(Provider.KAKAO, "social-1"))
                .willReturn(Optional.of(suspended));

        assertThatThrownBy(() -> authService.login(new LoginRequest(Provider.KAKAO, "social-access-token")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NOT_ACTIVE);
        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void 탈퇴한_회원은_refresh_token이_유효해도_재발급받을_수_없다() {
        Member withdrawn = createMember(2L, MemberStatus.INACTIVE);
        RefreshToken savedToken = RefreshToken.builder()
                .member(withdrawn)
                .token("old-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("2");
        given(jwtTokenProvider.parseClaims("old-refresh-token")).willReturn(claims);
        given(refreshTokenRepository.findByToken("old-refresh-token")).willReturn(Optional.of(savedToken));

        assertThatThrownBy(() -> authService.reissue(new ReissueRequest("old-refresh-token")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NOT_ACTIVE);
        verify(refreshTokenRepository).delete(savedToken);
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    void 정상_회원은_로그인하면_토큰이_발급된다() {
        Member active = createMember(3L, MemberStatus.ACTIVE);
        given(kakaoLoginClient.supports(Provider.KAKAO)).willReturn(true);
        given(kakaoLoginClient.fetchUserInfo("social-access-token"))
                .willReturn(new SocialUserInfo("social-3", "user3@test.com"));
        given(memberRepository.findByProviderAndSocialId(Provider.KAKAO, "social-3"))
                .willReturn(Optional.of(active));
        given(jwtTokenProvider.generateAccessToken(active)).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(active)).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenExpireMs()).willReturn(1_209_600_000L);

        var response = authService.login(new LoginRequest(Provider.KAKAO, "social-access-token"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(any());
    }

    private Member createMember(Long id, MemberStatus status) {
        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .socialId("social-" + id)
                .email("user" + id + "@test.com")
                .nickname("nick" + id)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "status", status);
        return member;
    }
}
