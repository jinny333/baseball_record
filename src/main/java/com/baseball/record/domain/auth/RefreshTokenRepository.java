package com.baseball.record.domain.auth;

import com.baseball.record.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByMemberAndToken(Member member, String token);

    void deleteByMember(Member member);
}
