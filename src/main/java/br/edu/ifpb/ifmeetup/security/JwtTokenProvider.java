package br.edu.ifpb.ifmeetup.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import br.edu.ifpb.ifmeetup.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String JWT_COOKIE_NAME = "jwt";
    private static final int COOKIE_EXPIRY = 7 * 24 * 60 * 60;
    
    private final SecretKey key;
    private final long tokenValidityInMilliseconds;
    private final boolean secure;
    private final String domain;

    public JwtTokenProvider(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long tokenValidityInMilliseconds,
            @Value("${jwt.cookie.secure:false}") boolean secure,
            @Value("${jwt.cookie.domain:localhost}") String domain) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
        this.secure = secure;
        this.domain = domain;
    }

    public String createToken(User user) {
        Map<String, Object> claims = Map.of(
            "id", user.getId().toString(),
            "email", user.getEmail(),
            "name", user.getFirstName() + " " + user.getLastName(),
            "roles", user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList())
        );

        UUID sessionId = UUID.randomUUID();
        
        Date validity = new Date(System.currentTimeMillis() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .claim("sid", sessionId.toString())
                .issuedAt(new Date())
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public void setTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .maxAge(COOKIE_EXPIRY)
                .domain(domain)
                .path("/")
                .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Token JWT adicionado ao cookie");
    }

    public void clearTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .maxAge(0)
                .domain(domain)
                .path("/")
                .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Token JWT removido do cookie");
    }

    public String resolveToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token JWT inválido ou expirado: {}", e.getMessage());
            return false;
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public UUID getSessionIdFromToken(String token) {
        return UUID.fromString(getClaimFromToken(token, claims -> claims.get("sid", String.class)));
    }
    
    public LocalDateTime getExpirationDateFromToken(String token) {
        Date expirationDate = getClaimFromToken(token, Claims::getExpiration);
        return Instant.ofEpochMilli(expirationDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
} 