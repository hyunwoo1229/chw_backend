package com.example.study3.controller;


import com.example.study3.domain.Member;
import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.MemberDto;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api")

public class AuthController {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthController(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody MemberDto dto) {
        Optional<Member> optionalMember = memberRepository.findByLoginId(dto.getLoginId());

        if (!optionalMember.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("존재하지 않는 아이디입니다"));
        }

        Member member = optionalMember.get();

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("비밀번호가 틀렸습니다"));
        }

        String token = jwtTokenProvider.createToken(member.getLoginId());
        return ResponseEntity
                .ok(new SuccessResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        return ResponseEntity.ok(new SuccessResponse("로그아웃 완료"));
    }
}
