package io.sphynx.server.controller;

import io.sphynx.server.dto.GenericResponse;
import io.sphynx.server.dto.auth.ResetPasswordRequest;
import io.sphynx.server.dto.auth.SignInRequest;
import io.sphynx.server.dto.auth.SignUpRequest;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.TokenType;
import io.sphynx.server.service.AuthService;
import io.sphynx.server.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        try {
            UserModel user = this.authService.register(signUpRequest);
            String token = jwtService.generateToken(user.getUserId(), TokenType.AUTH);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.CREATED.value(),
                            "User signed up successfully",
                            token
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while creating the user: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<GenericResponse<?>> signIn(
            @Valid @RequestBody SignInRequest signInRequest
    ) {
        try {
            UserModel user =  this.authService.authenticate(signInRequest);
            String token = this.jwtService.generateToken(user.getUserId(), TokenType.AUTH);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "User signed in successfully",
                            token
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while authenticating the user: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponse<?>> resetPassword(
            @RequestParam String resetToken,
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        try {
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

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while resetting password the user: " + e.getMessage(),
                            null
                            )
                    );
        }
    }
}
