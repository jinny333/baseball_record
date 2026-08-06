package com.baseball.record.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndSocialId(Provider provider, String socialId);

    boolean existsByProviderAndSocialId(Provider provider, String socialId);
}
