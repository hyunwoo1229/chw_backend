package com.example.study3.service;

import com.example.study3.domain.Member;
import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.MemberDto;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> login(MemberDto dto) {
        Optional<Member> optionalMember = memberRepository.findByLoginId(dto.getLoginId());

        if (optionalMember.isEmpty()) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("name", member.getName());

        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        // 토큰 무효화 로직이 있다면 여기 추가
        return ResponseEntity.ok(new SuccessResponse("로그아웃 완료"));
    }
}
