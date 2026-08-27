package com.baseball.record.domain.auth;

import com.baseball.record.domain.auth.dto.LoginRequest;
import com.baseball.record.domain.auth.dto.LogoutRequest;
import com.baseball.record.domain.auth.dto.ReissueRequest;
import com.baseball.record.domain.auth.dto.SignupRequest;
import com.baseball.record.domain.auth.dto.TokenResponse;
import com.baseball.record.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 URL(/api/v1/users/signup)이 함께 묶여 있는 이유는 명세상 signup이
 * 이 컨트롤러가 다루는 소셜 로그인 인증 흐름(login -> 미가입 시 signup)의 일부이기 때문이다.
 * Member 자체의 CRUD(/api/v1/users/me 등)는 별도 MemberController에서 다룬다.
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/api/v1/users/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/api/v1/auth/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }

    @PostMapping("/api/v1/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CurrentMemberId Long memberId, @Valid @RequestBody LogoutRequest request) {
        authService.logout(memberId, request.refreshToken());
    }
}
