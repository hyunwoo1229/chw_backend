package com.example.study3.service;

import com.example.study3.domain.Member;
import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.MemberDto;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.dto.TokenResponseDto;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
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

    @Transactional
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

        //토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getLoginId());

        // ✅ DB에 Refresh Token 저장
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("name", member.getName());

        return ResponseEntity.ok(result);
    }

    @Transactional // ✅ 추가
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            String loginId = jwtTokenProvider.getLoginId(token);
            memberRepository.findByLoginId(loginId).ifPresent(member -> {
                member.updateRefreshToken(null); // ✅ DB에서 Refresh Token 제거
                memberRepository.save(member);
            });
        }
        return ResponseEntity.ok(new SuccessResponse("로그아웃 완료"));
    }

    @Transactional
    public ResponseEntity<?> reissue(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("유효하지 않은 Refresh Token 입니다."));
        }

        // 2. DB에서 토큰을 찾아보고, 없을 경우 401 에러를 명시적으로 반환
        Optional<Member> memberOptional = memberRepository.findByRefreshToken(refreshToken);
        if (memberOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("제공된 Refresh Token과 일치하는 사용자가 없습니다."));
        }
        Member member = memberOptional.get();

        // 3. 새로운 토큰 생성 (Access Token + Refresh Token 둘 다)
        String newAccessToken = jwtTokenProvider.createAccessToken(member.getLoginId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getLoginId());

        // 4. DB에 새로운 Refresh Token 업데이트 (Refresh Token Rotation)
        member.updateRefreshToken(newRefreshToken);

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, newRefreshToken));
    }
}
