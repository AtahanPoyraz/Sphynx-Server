package io.sphynx.server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${security.jwt.secret_key}")
    private String jwtSecretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

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

    public String generateToken(String email) {
        UserModel user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User cannot found with email: " + email));

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + this.jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public UserModel extractClaimsFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }

        UUID userId = UUID.fromString(subject);
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User cannot found"));
    }

    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Invalid token");
            }

            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}
