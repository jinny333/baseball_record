package com.baseball.record.domain.review;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByIdAndMember_Id(Long id, Long memberId);

    boolean existsByRecord_Id(Long recordId);

    List<Review> findAllByStatus(ReviewStatus status);

    List<Review> findAllByStadiumAndStatus(String stadium, ReviewStatus status);

    @Query("""
            SELECT r FROM Review r
            WHERE r.status = com.baseball.record.domain.review.ReviewStatus.ACTIVE
              AND (:stadium IS NULL OR r.stadium = :stadium)
              AND (:section IS NULL OR r.section = :section)
              AND (:block IS NULL OR r.block = :block)
            """)
    Page<Review> searchActive(@Param("stadium") String stadium,
                               @Param("section") String section,
                               @Param("block") String block,
                               Pageable pageable);
}
