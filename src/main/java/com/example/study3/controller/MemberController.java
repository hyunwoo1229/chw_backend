package com.example.study3.controller;

import com.example.study3.domain.Member;
import com.example.study3.dto.*;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import com.example.study3.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor

public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody MemberDto dto) {
        return memberService.register(dto);
    }

    //소셜 로그인 후 추가 정보 저장
    @PostMapping("/update-extra")
    public ResponseEntity<?> updateExtra(@RequestBody MemberDto dto, Authentication authentication) {

        memberService.updateExtra(dto, authentication);
        return ResponseEntity.ok(new SuccessResponse("추가 정보 업데이트 완료"));
    }

    //내 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> getMyProfile(Authentication authentication) {
        ProfileResponseDto dto = memberService.getProfile(authentication);
        return ResponseEntity.ok(dto);
    }

    //비밀번호 변경
    @PutMapping("/profile/password")
    public ResponseEntity<Void> updatePassword(@RequestBody ChangePasswordRequest req, Authentication authentication) {
        memberService.changePassword(req, authentication);
        return ResponseEntity.ok().build();
    }
}