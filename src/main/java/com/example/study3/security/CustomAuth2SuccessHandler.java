package com.example.study3.security;

import com.example.study3.domain.Member;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid OAuth2 token.");
            return;
        }

        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // "google", "naver", "kakao"

        String loginId = extractLoginId(oAuth2User, provider);
        String name = extractName(oAuth2User, provider);

        // 사용자 정보가 DB에 없다면 회원가입 처리
        Optional<Member> existing = memberRepository.findByLoginId(loginId);
        Member user = existing.orElseGet(() -> {
            Member newMember = new Member();
            newMember.setLoginId(loginId);
            newMember.setName(name);
            newMember.setPassword(""); // 소셜 로그인은 비밀번호 필요 없음
            newMember.setProvider(provider);
            return memberRepository.save(newMember);
        });

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(loginId);

        // 리다이렉트할 URL (프론트에 토큰 전달)
        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth-success")
                .queryParam("token", token)
                .queryParam("name", URLEncoder.encode(user.getName(), StandardCharsets.UTF_8))
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String extractLoginId(OAuth2User oAuth2User, String provider) {
        if ("google".equals(provider)) {
            return oAuth2User.getAttribute("email");
        } else if ("naver".equals(provider)) {
            Map<String, Object> response = oAuth2User.getAttribute("response");
            return (String) response.get("email");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> account = oAuth2User.getAttribute("kakao_account");
            return (String) account.get("email");
        }
        return "unknown";
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        if ("google".equals(provider)) {
            return oAuth2User.getAttribute("name");
        } else if ("naver".equals(provider)) {
            Map<String, Object> response = oAuth2User.getAttribute("response");
            return (String) response.get("name");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> account = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) account.get("profile");
            return (String) profile.get("nickname");
        }
        return "Unknown";
    }
}
