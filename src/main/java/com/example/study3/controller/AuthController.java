package com.example.study3.controller;


import com.example.study3.domain.Member;
import com.example.study3.dto.MemberDto;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

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
        Member member = memberRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다"));
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다");
        }

        String token = jwtTokenProvider.createToken(member.getLoginId());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
