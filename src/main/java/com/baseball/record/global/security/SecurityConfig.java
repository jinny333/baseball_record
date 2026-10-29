package com.baseball.record.global.security;

import com.baseball.record.domain.member.MemberRepository;
import com.baseball.record.global.error.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 소셜 로그인은 Spring Security의 리다이렉트형 oauth2Login이 아니라,
 * 클라이언트가 이미 발급받은 provider access token을 우리 서버가 검증하는 방식(AuthService)이므로
 * 여기서는 순수하게 "JWT 검증 필터 + 경로별 인가 규칙"만 담당한다.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository,
                           ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/reissue", "/api/v1/users/signup").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(this::handleAuthenticationEntryPoint)
                        .accessDeniedHandler(this::handleAccessDenied))
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, memberRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleAuthenticationEntryPoint(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationException e) throws IOException {
        writeError(response, HttpStatus.UNAUTHORIZED, ErrorResponse.of("UNAUTHORIZED", "인증이 필요합니다."));
    }

    private void handleAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                     AccessDeniedException e) throws IOException {
        writeError(response, HttpStatus.FORBIDDEN, ErrorResponse.of("FORBIDDEN", "접근 권한이 없습니다."));
    }

    private void writeError(HttpServletResponse response, HttpStatus status, ErrorResponse body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
