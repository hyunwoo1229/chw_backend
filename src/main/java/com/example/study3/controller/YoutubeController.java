package com.example.study3.controller;

import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.service.YoutubeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final YoutubeService youtubeService;

    // 유튜브 계정 연결을 위한 Google OAuth 로그인 URL 생성 및 리디렉션
    @GetMapping("/connect")
    public void connectYoutube(@RequestParam String token, HttpServletResponse response) throws IOException {
        try {
            String redirectUrl = youtubeService.generateGoogleAuthUrl(token);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5173/error?msg=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    // 구글에서 authorization code를 전달받아 access token 교환을 시작하는 콜백 핸들러
    @GetMapping("/oauth2/callback")
    public void handleYoutubeCallback(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws IOException {
        try {
            youtubeService.handleCallback(code, state);
            response.sendRedirect("http://localhost:5173/upload/finish");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:5173/error?msg=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    //유튜브 업로드
    @PostMapping("/{id}")
    public ResponseEntity<?> uploadToYoutube(@PathVariable Long id, Authentication authentication) {
        try {
            String youtubeUrl = youtubeService.uploadUserBoardToYoutube(id, authentication);
            return ResponseEntity.ok(new SuccessResponse(youtubeUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("유튜브 업로드 실패: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("업로드 중 알 수 없는 오류가 발생했습니다."));
        }
    }
}
