package com.example.study3.service;

import com.example.study3.domain.Member;
import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.MemberDto;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> register(MemberDto dto) {
        if (memberRepository.findByLoginId(dto.getLoginId()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("이미 존재하는 아이디입니다"));
        }

        Member member = new Member();
        member.setLoginId(dto.getLoginId());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setName(dto.getName());
        member.setProvider("form");
        member.setAge(dto.getAge());
        member.setGender(dto.getGender());
        member.setCountry(dto.getCountry());
        memberRepository.save(member);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse("회원가입 성공"));
    }
}
