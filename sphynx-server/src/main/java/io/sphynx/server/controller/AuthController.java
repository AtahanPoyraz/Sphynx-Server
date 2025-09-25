package io.sphynx.server.controller;

import io.micrometer.common.lang.NonNull;
import io.sphynx.server.dto.GenericResponse;
import io.sphynx.server.dto.auth.ResetPasswordRequest;
import io.sphynx.server.dto.auth.SignInRequest;
import io.sphynx.server.dto.auth.SignUpRequest;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.TokenType;
import io.sphynx.server.service.AuthService;
import io.sphynx.server.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(
            AuthService authService,
            JwtService jwtService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<GenericResponse<?>> signUp(
            @NonNull HttpServletResponse  httpServletResponse,
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        UserModel user = this.authService.register(signUpRequest);
        String token = jwtService.generateToken(user.getUserId(), TokenType.AUTH);

        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse<>(
                        HttpStatus.CREATED.value(),
                        "User signed up successfully",
                        token
                        )
                );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<GenericResponse<?>> signIn(
            @NonNull HttpServletResponse  httpServletResponse,
            @Valid @RequestBody SignInRequest signInRequest
    ) {
        UserModel user =  this.authService.authenticate(signInRequest);
        String token = this.jwtService.generateToken(user.getUserId(), TokenType.AUTH);

        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(
                        HttpStatus.OK.value(),
                        "User signed in successfully",
                        token
                        )
                );
    }

    @GetMapping("/sign-out")
    public ResponseEntity<GenericResponse<?>> signOut(
            @NonNull HttpServletResponse  httpServletResponse
    ) {
        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(
                        HttpStatus.OK.value(),
                        "User signed out successfully",
                        null
                        )
                );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponse<?>> resetPassword(
            @RequestParam String resetToken,
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        if (!jwtService.isTokenValid(resetToken, TokenType.RESET)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericResponse<>(
                            HttpStatus.BAD_REQUEST.value(),
                            "Reset token is invalid or has expired",
                            null
                            )
                    );
        }

        UserModel user = this.jwtService.extractUserFromToken(resetToken, TokenType.RESET);
        this.authService.resetPassword(user, resetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(
                        HttpStatus.OK.value(),
                        "Password reset successfully",
                        null
                        )
                );
    }
}
