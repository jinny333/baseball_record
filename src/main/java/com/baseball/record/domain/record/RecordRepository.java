package com.baseball.record.domain.record;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {

    Optional<Record> findByIdAndMember_Id(Long id, Long memberId);

    Page<Record> findAllByMember_Id(Long memberId, Pageable pageable);
}
