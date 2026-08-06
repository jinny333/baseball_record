package com.baseball.record.domain.member.dto;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.member.MemberStatus;
import com.baseball.record.domain.member.Provider;
import com.baseball.record.domain.member.Role;
import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        Provider provider,
        String email,
        String nickname,
        String favoriteTeam,
        String birth,
        String phoneNumber,
        Role role,
        MemberStatus status,
        LocalDateTime createdAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.getNickname(),
                member.getFavoriteTeam(),
                member.getBirth(),
                member.getPhoneNumber(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt());
    }
}
