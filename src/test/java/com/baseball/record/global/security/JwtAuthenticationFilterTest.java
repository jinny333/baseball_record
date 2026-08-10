package com.baseball.record.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.member.MemberStatus;
import com.baseball.record.domain.member.Provider;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-for-jwt-filter-unit-test-min-32-bytes";

    private final JwtTokenProvider jwtTokenProvider =
            new JwtTokenProvider(new JwtProperties(SECRET, 60_000, 120_000));

    @Mock
    private MemberRepository memberRepository;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, memberRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ACTIVE_회원의_토큰이면_인증정보가_설정된다() throws Exception {
        Member member = createMember(1L);
        given(memberRepository.findStatusById(1L)).willReturn(Optional.of(MemberStatus.ACTIVE));

        MockHttpServletResponse response = runFilter(jwtTokenProvider.generateAccessToken(member));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void 정지된_회원의_토큰이면_인증정보가_설정되지_않는다() throws Exception {
        Member member = createMember(2L);
        given(memberRepository.findStatusById(2L)).willReturn(Optional.of(MemberStatus.SUSPENDED));

        runFilter(jwtTokenProvider.generateAccessToken(member));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void 탈퇴해_존재하지_않는_회원의_토큰이면_인증정보가_설정되지_않는다() throws Exception {
        Member member = createMember(3L);
        given(memberRepository.findStatusById(3L)).willReturn(Optional.empty());

        runFilter(jwtTokenProvider.generateAccessToken(member));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private MockHttpServletResponse runFilter(String accessToken) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        return response;
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
}
