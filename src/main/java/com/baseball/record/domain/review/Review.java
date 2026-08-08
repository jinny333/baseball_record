package com.baseball.record.domain.review;

import com.baseball.record.domain.member.Member;
import com.baseball.record.domain.record.Record;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

/**
 * 단방향 연관관계: Record -> Review, Member -> Review 역방향 컬렉션은 두지 않는다.
 * (Record/Report와 동일한 원칙 — 필요하면 Repository 조회로 해결)
 * stadium/section/block/rowNum/seatNum은 연결된 Record 값을 생성 시점에 그대로 복사해 저장한다.
 * 클라이언트 입력을 신뢰하지 않고, 필터링·검색 최적화를 위한 비정규화 컬럼으로만 쓴다.
 */
@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

    private static final String KEYWORD_DELIMITER = ",";
    private static final String VECTOR_TERM_DELIMITER = ":";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false, unique = true)
    private Record record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(name = "view_image", length = 500)
    private String viewImage;

    @Lob
    @Column(name = "review_content", nullable = false)
    private String reviewContent;

    @Getter(AccessLevel.NONE)
    @Column(length = 255)
    private String keywords;

    // 리뷰 저장 이후 비동기로 계산되는 형태소 분석 기반 추천 벡터(용어:빈도수). 코사인 유사도 추천에서 사용한다.
    @Getter(AccessLevel.NONE)
    @Column(name = "keyword_vector", length = 1000)
    private String keywordVector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status;

    @Column(length = 30)
    private String stadium;

    @Column(length = 30)
    private String section;

    @Column(length = 30)
    private String block;

    @Column(name = "row_num", length = 10)
    private String rowNum;

    @Column(name = "seat_num", length = 10)
    private String seatNum;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Review(Record record, Member member, String viewImage, String reviewContent, List<String> keywords,
                    String stadium, String section, String block, String rowNum, String seatNum) {
        this.record = record;
        this.member = member;
        this.viewImage = viewImage;
        this.reviewContent = reviewContent;
        this.keywords = joinKeywords(keywords);
        this.status = ReviewStatus.ACTIVE;
        this.stadium = stadium;
        this.section = section;
        this.block = block;
        this.rowNum = rowNum;
        this.seatNum = seatNum;
    }

    public List<String> getKeywords() {
        if (!StringUtils.hasText(keywords)) {
            return List.of();
        }
        return Arrays.asList(keywords.split(KEYWORD_DELIMITER));
    }

    public void update(String reviewContent, List<String> keywords) {
        this.reviewContent = reviewContent;
        this.keywords = joinKeywords(keywords);
    }

    public void blind() {
        this.status = ReviewStatus.BLIND;
    }

    public Map<String, Long> getKeywordVector() {
        if (!StringUtils.hasText(keywordVector)) {
            return Map.of();
        }
        return Arrays.stream(keywordVector.split(KEYWORD_DELIMITER))
                .map(pair -> pair.split(VECTOR_TERM_DELIMITER))
                .collect(Collectors.toMap(pair -> pair[0], pair -> Long.parseLong(pair[1])));
    }

    public void updateKeywordVector(Map<String, Long> keywordVector) {
        this.keywordVector = keywordVector.entrySet().stream()
                .map(entry -> entry.getKey() + VECTOR_TERM_DELIMITER + entry.getValue())
                .collect(Collectors.joining(KEYWORD_DELIMITER));
    }

    private static String joinKeywords(List<String> keywords) {
        return keywords == null ? null : String.join(KEYWORD_DELIMITER, keywords);
    }
}
