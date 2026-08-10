package com.baseball.record.domain.record;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {

    Optional<Record> findByIdAndMember_Id(Long id, Long memberId);

    Page<Record> findAllByMember_Id(Long memberId, Pageable pageable);

    // 취소된 경기는 승/패/무 통계에서 제외한다.
    List<Record> findAllByMember_IdAndMatchResultNot(Long memberId, MatchResult matchResult);
}
