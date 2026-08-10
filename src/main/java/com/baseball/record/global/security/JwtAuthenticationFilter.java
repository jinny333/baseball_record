package com.baseball.record.global.security;

import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.domain.member.MemberStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)
                && jwtTokenProvider.isAccessToken(token)) {
            Long memberId = jwtTokenProvider.getMemberId(token);

            // access token의 role 클레임은 발급 시점에 고정되어, 발급 이후 정지/탈퇴된 회원이라도
            // 토큰 자체는 만료(최대 30분) 전까지 유효하게 검증된다. 최신 회원 상태를 매 요청마다
            // 다시 확인해야만 정지 조치가 즉시 반영된다(status만 조회하는 경량 쿼리로 오버헤드를 최소화).
            if (isActiveMember(memberId)) {
                String role = jwtTokenProvider.getRole(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        memberId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isActiveMember(Long memberId) {
        return memberRepository.findStatusById(memberId)
                .filter(MemberStatus.ACTIVE::equals)
                .isPresent();
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
