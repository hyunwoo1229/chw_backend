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

@RestController
@RequestMapping("/api")

public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberController(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberDto dto) {
        if (memberRepository.findByLoginId(dto.getLoginId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 아이디입니다");
        }
        Member member = new Member();
        member.setLoginId(dto.getLoginId());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setName(dto.getName());
        memberRepository.save(member);

        return ResponseEntity.ok("회원가입 성공");
    }



}
