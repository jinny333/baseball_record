package com.baseball.record.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * JwtAuthenticationFilter가 SecurityContext에 심어두는 principal(Long memberId)을 꺼내는 의도를
 * 타입 시그니처만으로 드러내기 위한 @AuthenticationPrincipal의 alias.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentMemberId {
}
