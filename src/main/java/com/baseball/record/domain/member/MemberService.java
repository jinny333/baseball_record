package com.baseball.record.domain.member;

import com.baseball.record.domain.auth.RefreshTokenRepository;
import com.baseball.record.domain.member.dto.MemberResponse;
import com.baseball.record.domain.member.dto.MemberUpdateRequest;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public MemberService(MemberRepository memberRepository, RefreshTokenRepository refreshTokenRepository) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public MemberResponse getMyProfile(Long memberId) {
        return MemberResponse.from(findMember(memberId));
    }

    @Transactional
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = findMember(memberId);
        member.updateProfile(request.nickname(), request.favoriteTeam());
        return MemberResponse.from(member);
    }

    /**
     * 즉시 삭제 대신 상태만 INACTIVE로 전환하는 소프트 삭제.
     * Record/Review 등 다른 도메인이 회원을 FK로 참조하므로 하드 삭제는 그쪽 데이터까지 함께 고려해야 한다.
     */
    @Transactional
    public void withdraw(Long memberId) {
        Member member = findMember(memberId);
        member.changeStatus(MemberStatus.INACTIVE);
        refreshTokenRepository.deleteByMember(member);
    }

    public Page<MemberResponse> getMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberResponse::from);
    }

    /**
     * (관리자 전용) 신고 누적 등의 사유로 악성 유저를 SUSPENDED로 전환하거나,
     * 오판정된 계정을 다시 ACTIVE로 복구하는 등 회원 상태를 직접 제어한다.
     * ACTIVE가 아닌 상태로 전환할 때는 refresh token을 모두 폐기해, 이미 로그인된 세션이
     * /auth/reissue로 계속 새 access token을 받아가는 것을 막는다.
     */
    @Transactional
    public MemberResponse updateStatus(Long memberId, MemberStatus status) {
        Member member = findMember(memberId);
        member.changeStatus(status);
        if (status != MemberStatus.ACTIVE) {
            refreshTokenRepository.deleteByMember(member);
        }
        return MemberResponse.from(member);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
