package com.baseball.record.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "social_id", nullable = false, length = 100)
    private String socialId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "favorite_team", length = 30)
    private String favoriteTeam;

    @Column(length = 20)
    private String birth;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Member(Provider provider, String socialId, String email, String nickname,
                    String favoriteTeam, String birth, String phoneNumber) {
        this.provider = provider;
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.favoriteTeam = favoriteTeam;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.role = Role.USER;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateProfile(String nickname, String favoriteTeam) {
        this.nickname = nickname;
        this.favoriteTeam = favoriteTeam;
    }

    public void changeStatus(MemberStatus status) {
        this.status = status;
    }
}
