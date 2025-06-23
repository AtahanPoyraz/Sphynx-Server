package io.sphynx.server.controller;

import io.sphynx.server.dto.GenericResponse;
import io.sphynx.server.dto.auth.SignInRequest;
import io.sphynx.server.dto.auth.SignUpRequest;
import io.sphynx.server.service.AuthService;
import io.sphynx.server.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            this.authService.register(signUpRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "User signed up successfully",
                            null
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
            this.authService.authenticate(signInRequest);
            String token = this.jwtService.generateToken(signInRequest.getEmail());
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
}
