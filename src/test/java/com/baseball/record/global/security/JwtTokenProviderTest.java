package com.baseball.record.global.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.Provider;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-jwt-unit-test-minimum-32-bytes";

    @Test
    void 액세스_토큰은_회원ID와_권한을_담아_발급되고_검증된다() {
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(SECRET, 60_000, 120_000));
        Member member = createMember(1L);

        String accessToken = provider.generateAccessToken(member);

        assertTrue(provider.validateToken(accessToken));
        assertEquals(1L, provider.getMemberId(accessToken));
        assertEquals("USER", provider.getRole(accessToken));
    }

    @Test
    void 리프레시_토큰은_권한_클레임을_담지_않는다() {
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(SECRET, 60_000, 120_000));
        Member member = createMember(2L);

        String refreshToken = provider.generateRefreshToken(member);

        assertEquals(2L, provider.getMemberId(refreshToken));
        assertNull(provider.getRole(refreshToken));
    }

    @Test
    void 만료된_토큰은_검증에_실패하고_파싱시_예외를_던진다() throws InterruptedException {
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(SECRET, 1, 1));
        Member member = createMember(3L);

        String accessToken = provider.generateAccessToken(member);
        Thread.sleep(10);

        assertFalse(provider.validateToken(accessToken));
        assertThrows(ExpiredJwtException.class, () -> provider.parseClaims(accessToken));
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
