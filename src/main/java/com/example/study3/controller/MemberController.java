package com.example.study3.controller;

import com.example.study3.domain.Member;
import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.MemberDto;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
//@RequestMapping("/api")

public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberController(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider
            , RestTemplate restTemplate) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody MemberDto dto) {
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
        memberRepository.save(member);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse("회원가입 성공"));
    }


    private static final String CLIENT_ID = "1067642253282-b0oi07bo4l4bjhdqtkrndmdr31ekj5cc.apps.googleusercontent.com";
    @Value("${client.secret}")
    private String CLIENT_SECRET;
    private static final String REDIRECT_URI = "http://localhost:8080/oauth2/callback/youtube";

    @GetMapping("/api/connect/youtube")
    public void connectYoutube(@RequestParam String token, HttpServletResponse response) throws IOException {
        if (token == null || token.isBlank()) {
            response.sendRedirect("http://localhost:5173/error?msg=토큰이 없습니다");
            return;
        }

        String loginId = jwtTokenProvider.getLoginId(token); // ✅ subject로부터 로그인 ID 추출
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 없음"));

        String stateJwt = jwtTokenProvider.createToken(member.getLoginId());

        String redirectUrl = UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code")
                .queryParam("scope", "https://www.googleapis.com/auth/youtube.upload profile email")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", stateJwt)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }


    @GetMapping("/oauth2/callback/youtube")
    public void handleYoutubeCallback(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws IOException {
        try {

            // access token 요청
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", CLIENT_ID);
            params.add("client_secret", CLIENT_SECRET);
            params.add("redirect_uri", REDIRECT_URI);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse = new RestTemplate().postForEntity(
                    "https://oauth2.googleapis.com/token",
                    request,
                    Map.class
            );

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // JWT로부터 유저 식별
            Claims claims = jwtTokenProvider.parseClaims(state);
            String loginId = claims.getSubject();

            Member member = memberRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

            member.setYoutubeAccessToken(accessToken);
            memberRepository.save(member);

            response.sendRedirect("http://localhost:5173/upload/finish");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:5173/error?msg=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

}