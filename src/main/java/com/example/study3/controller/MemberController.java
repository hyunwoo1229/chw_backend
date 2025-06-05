package com.example.study3.controller;

import com.example.study3.domain.Member;
import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.MemberDto;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import com.example.study3.service.MemberService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/update-extra")
    public ResponseEntity<?> updateExtra(@RequestBody MemberDto dto, Authentication authentication) {

        System.out.println("🔥 진입 성공");

        if (authentication == null) {
            System.out.println("❌ 인증 정보 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 없음");
        }

        System.out.println("✅ 인증된 사용자: " + authentication.getName());

        String loginId = authentication.getName();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다."));

        member.setAge(dto.getAge());
        member.setGender(dto.getGender());
        member.setCountry(dto.getCountry());
        memberRepository.save(member);

        return ResponseEntity.ok(new SuccessResponse("추가 정보 업데이트 완료"));
    }
}