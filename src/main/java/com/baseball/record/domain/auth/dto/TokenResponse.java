package com.baseball.record.domain.auth.dto;

public record TokenResponse(String accessToken, String refreshToken) {
}
