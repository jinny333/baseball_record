package com.baseball.record.domain.record;

import com.baseball.record.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    // 단방향 다대일: Member -> Record 역방향 컬렉션은 절대 추가하지 않는다.
    // (양방향 연관관계는 연관관계 편의 메서드/무한루프/영속성 전이 실수를 유발하기 쉽고,
    //  Member 입장에서 Record 목록이 필요하면 Repository 조회로 해결한다.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(nullable = false, length = 30)
    private String stadium;

    @Column(name = "home_team", nullable = false, length = 30)
    private String homeTeam;

    @Column(name = "away_team", nullable = false, length = 30)
    private String awayTeam;

    @Column(name = "my_team", nullable = false, length = 30)
    private String myTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_result", nullable = false, length = 20)
    private MatchResult matchResult;

    @Column(length = 30)
    private String section;

    @Column(length = 30)
    private String block;

    @Column(name = "row_num", length = 10)
    private String rowNum;

    @Column(name = "seat_num", length = 10)
    private String seatNum;

    @Lob
    @Column(name = "diary_comment")
    private String diaryComment;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder
    private Record(Member member, LocalDate matchDate, String stadium, String homeTeam,
                    String awayTeam, String myTeam, MatchResult matchResult, String section,
                    String block, String rowNum, String seatNum, String diaryComment, String imageUrl) {
        this.member = member;
        this.matchDate = matchDate;
        this.stadium = stadium;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.myTeam = myTeam;
        this.matchResult = matchResult;
        this.section = section;
        this.block = block;
        this.rowNum = rowNum;
        this.seatNum = seatNum;
        this.diaryComment = diaryComment;
        this.imageUrl = imageUrl;
    }

    public void update(MatchResult matchResult, String diaryComment, String imageUrl) {
        this.matchResult = matchResult;
        this.diaryComment = diaryComment;
        this.imageUrl = imageUrl;
    }
}
