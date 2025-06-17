package com.example.study3.security.jwt;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Base64;
import java.util.Date;


@Component

public class JwtTokenProvider {

    @Value("${keySecret}")
    private String keySecret;
    private final long ACCESS_TOKEN_EXPIRATION_MS = 36000000L;
    private final long REFRESH_TOKEN_EXPIRATION_MS = 1209600000L; // 2주
    private Key key;

    @PostConstruct
    protected void init() {
        byte[] secretBytes = Base64.getEncoder().encode(keySecret.getBytes());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String createAccessToken(String loginId) {
        return createToken(loginId, ACCESS_TOKEN_EXPIRATION_MS);
    }

    public String createRefreshToken(String loginId) {
        return createToken(loginId, REFRESH_TOKEN_EXPIRATION_MS);
    }

    private String createToken(String loginId, long expirationMs) {
        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String getLoginId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }
        // --- 어떤 종류의 예외 때문에 실패하는지 로그로 확인 ---
        catch (SecurityException | MalformedJwtException e) {
            System.err.println("Invalid JWT Token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("Expired JWT Token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Unsupported JWT Token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // 이 예외는 보통 토큰이 비어있거나(null) 형식이 아닐 때 발생합니다.
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        // ------------------------------------------------
        return false;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key) // secretKey는 기존 서명 키
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("JWT 파싱 실패: " + e.getMessage());
        }
    }
}
