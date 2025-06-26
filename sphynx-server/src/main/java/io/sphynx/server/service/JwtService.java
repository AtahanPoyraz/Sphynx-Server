package io.sphynx.server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.TokenType;
import io.sphynx.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {
    @Value("${security.jwt.secret_key}")
    private String jwtSecretKey;

    @Value("${security.jwt.auth.expiration}")
    private long jwtAuthExpiration;

    @Value("${security.jwt.reset.expiration}")
    private long jwtResetExpiration;

    private final UserRepository userRepository;

    @Autowired
    public JwtService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    private SecretKey getSignInKey(String jwtSecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(String subject, Map<String, Object> claims, Long expireTime, SecretKey secretKey) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    private Claims extractClaimsFromToken(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(UUID id, TokenType tokenType) {
        SecretKey secretKey = this.getSignInKey(this.jwtSecretKey);
        return switch (tokenType) {
            case AUTH -> this.generateToken(id.toString(), Map.of("type", tokenType.name()), this.jwtAuthExpiration, secretKey);
            case RESET -> this.generateToken(id.toString(), Map.of("type", tokenType.name()), this.jwtResetExpiration, secretKey);
        };
    }

    public UserModel extractUserFromToken(String token, TokenType tokenType) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        SecretKey secretKey = this.getSignInKey(this.jwtSecretKey);
        Claims claims = this.extractClaimsFromToken(token, secretKey);

        if (!tokenType.name().equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Token type mismatch");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Token subject (user ID) is missing");
        }

        UUID userId = UUID.fromString(subject);
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public boolean isTokenValid(String token, TokenType tokenType) {
        try {
            if (token == null || token.isBlank()) {
                return false;
            }

            SecretKey secretKey = this.getSignInKey(this.jwtSecretKey);
            Claims claims = this.extractClaimsFromToken(token, secretKey);

            String typeClaim = claims.get("type", String.class);
            if (!tokenType.name().equalsIgnoreCase(typeClaim)) {
                return false;
            }

            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());

        } catch (Exception e) {
            log.debug("Invalid token: {}", e.getMessage());
            return false;
        }
    }
}
