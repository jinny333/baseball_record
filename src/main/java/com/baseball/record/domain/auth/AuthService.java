package com.baseball.record.domain.auth;

import com.baseball.record.domain.auth.dto.LoginRequest;
import com.baseball.record.domain.auth.dto.ReissueRequest;
import com.baseball.record.domain.auth.dto.SignupRequest;
import com.baseball.record.domain.auth.dto.TokenResponse;
import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.member.MemberStatus;
import com.baseball.record.domain.member.Provider;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import com.baseball.record.global.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AuthService {

    private final List<SocialLoginClient> socialLoginClients;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(List<SocialLoginClient> socialLoginClients, MemberRepository memberRepository,
                        RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.socialLoginClients = socialLoginClients;
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse login(LoginRequest request) {
        SocialUserInfo userInfo = resolveClient(request.provider()).fetchUserInfo(request.accessToken());

        Member member = memberRepository.findByProviderAndSocialId(request.provider(), userInfo.socialId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SIGNUP_REQUIRED));
        validateActive(member);

        return issueTokens(member);
    }

    public TokenResponse signup(SignupRequest request) {
        SocialUserInfo userInfo = resolveClient(request.provider()).fetchUserInfo(request.accessToken());

        if (memberRepository.existsByProviderAndSocialId(request.provider(), userInfo.socialId())) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }
        if (!StringUtils.hasText(userInfo.email())) {
            throw new BusinessException(ErrorCode.SOCIAL_EMAIL_REQUIRED);
        }

        Member member = Member.builder()
                .provider(request.provider())
                .socialId(userInfo.socialId())
                .email(userInfo.email())
                .nickname(request.nickname())
                .favoriteTeam(request.favoriteTeam())
                .birth(request.birth())
                .phoneNumber(request.phoneNumber())
                .build();
        memberRepository.save(member);

        return issueTokens(member);
    }

    public TokenResponse reissue(ReissueRequest request) {
        String refreshToken = request.refreshToken();
        Claims claims = parseRefreshTokenClaims(refreshToken);
        Long memberId = Long.valueOf(claims.getSubject());

        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> token.getMember().getId().equals(memberId))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        refreshTokenRepository.delete(savedToken);
        if (savedToken.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        validateActive(savedToken.getMember());

        return issueTokens(savedToken.getMember());
    }

    public void logout(Long memberId, String refreshToken) {
        Member member = memberRepository.getReferenceById(memberId);
        refreshTokenRepository.deleteByMemberAndToken(member, refreshToken);
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtTokenProvider.generateAccessToken(member);
        String refreshToken = jwtTokenProvider.generateRefreshToken(member);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpireMs())))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    // 탈퇴(INACTIVE)/정지(SUSPENDED) 회원은 refresh token이 살아있더라도 로그인·재발급으로
    // 새 access token을 받을 수 없어야 한다. 회원 상태를 여기서 확인해 즉시 차단한다.
    private void validateActive(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_ACTIVE);
        }
    }

    private SocialLoginClient resolveClient(Provider provider) {
        return socialLoginClients.stream()
                .filter(client -> client.supports(provider))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER));
    }

    private Claims parseRefreshTokenClaims(String refreshToken) {
        try {
            return jwtTokenProvider.parseClaims(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
