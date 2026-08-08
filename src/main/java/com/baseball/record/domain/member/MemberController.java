package com.baseball.record.domain.member;

import com.baseball.record.domain.member.dto.MemberResponse;
import com.baseball.record.domain.member.dto.MemberStatusUpdateRequest;
import com.baseball.record.domain.member.dto.MemberUpdateRequest;
import com.baseball.record.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/api/v1/users/me")
    public MemberResponse getMe(@CurrentMemberId Long memberId) {
        return memberService.getMyProfile(memberId);
    }

    @PatchMapping("/api/v1/users/me")
    public MemberResponse updateMe(@CurrentMemberId Long memberId,
                                    @Valid @RequestBody MemberUpdateRequest request) {
        return memberService.updateProfile(memberId, request);
    }

    @DeleteMapping("/api/v1/users/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@CurrentMemberId Long memberId) {
        memberService.withdraw(memberId);
    }

    @GetMapping("/api/v1/admin/users")
    public Page<MemberResponse> getMembers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return memberService.getMembers(pageable);
    }

    @PatchMapping("/api/v1/admin/users/{userId}/status")
    public MemberResponse updateMemberStatus(@PathVariable Long userId,
                                              @Valid @RequestBody MemberStatusUpdateRequest request) {
        return memberService.updateStatus(userId, request.status());
    }
}
