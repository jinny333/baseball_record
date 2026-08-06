package com.baseball.record.global.security;

import com.baseball.record.domain.member.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long accessTokenExpireMs;
    private final long refreshTokenExpireMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireMs = jwtProperties.accessTokenExpireMs();
        this.refreshTokenExpireMs = jwtProperties.refreshTokenExpireMs();
    }

    public String generateAccessToken(Member member) {
        return generateToken(member, accessTokenExpireMs, true);
    }

    public String generateRefreshToken(Member member) {
        return generateToken(member, refreshTokenExpireMs, false);
    }

    public long getRefreshTokenExpireMs() {
        return refreshTokenExpireMs;
    }

    public Long getMemberId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    /**
     * 필터 등 "유효한가 아닌가"만 필요한 호출부용. 만료/서명 오류를 구분해야 하는 곳(재발급 로직)은
     * {@link #parseClaims(String)}을 직접 호출해 jjwt 예외를 잡아 상황별로 처리한다.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String generateToken(Member member, long expireMs, boolean includeRole) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMs);

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .issuedAt(now)
                .expiration(expiry);

        if (includeRole) {
            builder.claim(CLAIM_ROLE, member.getRole().name());
        }

        return builder.signWith(key).compact();
    }
}
