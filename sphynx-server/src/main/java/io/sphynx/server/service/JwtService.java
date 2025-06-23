package io.sphynx.server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
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

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String requiredType) {
        UserModel user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User cannot found with email: " + email));

        return switch (requiredType.toLowerCase()) {
            case ("auth") -> Jwts.builder()
                    .subject(user.getUserId().toString())
                    .claim("type", "auth")
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + this.jwtAuthExpiration))
                    .signWith(getSignInKey())
                    .compact();

            case ("reset") -> Jwts.builder()
                    .subject(user.getUserId().toString())
                    .claim("type", "reset")
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + this.jwtResetExpiration))
                    .signWith(getSignInKey())
                    .compact();

            default -> throw new IllegalArgumentException("Invalid token type");
        };
    }

    public UserModel extractClaimsFromToken(String token, String requiredType) {
        log.info("------------------------------------------------------o "  + requiredType);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get("type", String.class);
        if (!requiredType.equals(tokenType)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        UUID userId = UUID.fromString(subject);
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User cannot found"));
    }

    public boolean isTokenValid(String token, String requiredType) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!requiredType.equals(claims.get("type", String.class))) {
                return false;
            }

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}
