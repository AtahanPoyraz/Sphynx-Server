package io.sphynx.server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.repository.UserRepository;
import io.sphynx.server.util.JwtUtil;
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


    public String generateToken(UUID id, String requiredType) {
        SecretKey secretKey = JwtUtil.getSignInKey(this.jwtSecretKey);
        return switch (requiredType.toLowerCase()) {
            case ("auth") -> JwtUtil.generateToken(
                    id.toString(),
                    Map.of("type", "auth"),
                    this.jwtAuthExpiration,
                    secretKey
            );

            case ("reset") -> JwtUtil.generateToken(
                    id.toString(),
                    Map.of("type", "reset"),
                    this.jwtResetExpiration,
                    secretKey
            );

            default -> throw new IllegalArgumentException("Invalid token type");
        };
    }

    public UserModel extractClaimsFromToken(String token, String requiredType) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        SecretKey secretKey = JwtUtil.getSignInKey(this.jwtSecretKey);
        Claims claims = JwtUtil.extractClaimsFromToken(token, secretKey);
        String tokenType = claims.get("type", String.class);
        if (!requiredType.toLowerCase().equals(tokenType)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        UUID userId = UUID.fromString(subject);
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public boolean isTokenValid(String token, String requiredType) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            SecretKey secretKey = JwtUtil.getSignInKey(this.jwtSecretKey);
            Claims claims = JwtUtil.extractClaimsFromToken(token, secretKey);
            if (!requiredType.toLowerCase().equals(claims.get("type", String.class))) {
                return false;
            }

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}
