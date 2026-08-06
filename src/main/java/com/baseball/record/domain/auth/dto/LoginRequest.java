package com.baseball.record.domain.auth.dto;

import com.baseball.record.domain.member.Provider;

public record LoginRequest(Provider provider, String accessToken) {
}
