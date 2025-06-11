package com.example.study3.service;

import com.example.study3.domain.Member;
import com.example.study3.dto.*;
import com.example.study3.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
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

    //내 정보 조회
    public ProfileResponseDto getProfile(Authentication auth) {
        String loginId = (String) auth.getPrincipal();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유요하지 않은 계정입니다."));
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setLoginId(loginId);
        dto.setName(member.getName());
        dto.setAge(member.getAge());
        dto.setGender(member.getGender());
        dto.setCountry(member.getCountry());
        dto.setProvider(member.getProvider());
        return dto;
    }

    //비밀번호 변경
    public void changePassword(ChangePasswordRequest req, Authentication auth) {
        String loginId = (String) auth.getPrincipal();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
        if(!passwordEncoder.matches(req.getOldPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        member.setPassword(passwordEncoder.encode(req.getNewPassword()));
        memberRepository.save(member);
    }
}
