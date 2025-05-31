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
