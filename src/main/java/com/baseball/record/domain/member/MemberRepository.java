package com.baseball.record.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndSocialId(Provider provider, String socialId);

    boolean existsByProviderAndSocialId(Provider provider, String socialId);

    // JwtAuthenticationFilter가 매 요청마다 회원 상태를 재확인할 때 쓰는 경량 조회.
    // 전체 엔티티 대신 status 컬럼 하나만 가져와 요청당 오버헤드를 최소화한다.
    @Query("SELECT m.status FROM Member m WHERE m.id = :id")
    Optional<MemberStatus> findStatusById(@Param("id") Long id);
}
