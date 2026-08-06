package com.baseball.record.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record NaverUserResponse(String resultcode, String message, NaverAccount response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverAccount(String id, String email) {
    }
}
