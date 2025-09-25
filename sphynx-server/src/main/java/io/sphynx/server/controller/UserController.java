package io.sphynx.server.controller;

import io.sphynx.server.dto.GenericResponse;
import io.sphynx.server.dto.user.CreateUserRequest;
import io.sphynx.server.dto.user.UpdateUserByIdRequest;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.TokenType;
import io.sphynx.server.service.JwtService;
import io.sphynx.server.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public UserController(
            UserService userService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<GenericResponse<?>> me(HttpServletRequest servletRequest) {
        String token = null;

        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new GenericResponse<>(
                            HttpStatus.UNAUTHORIZED.value(),
                            "JWT cookie is missing or empty",
                            null
                    ));
        }

        UserModel user = this.jwtService.extractUserFromToken(token, TokenType.AUTH);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(
                        HttpStatus.OK.value(),
                        "User fetched successfully",
                        user
                ));
    }

    @GetMapping("/get")
    public ResponseEntity<GenericResponse<?>> getUser(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String email,
            @ParameterObject Pageable pageable
    ) {
        if (userId != null) {
            UserModel user = this.userService.getUserByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "User fetched successfully",
                            user
                            )
                    );
        }

        if (email != null) {
            UserModel user = this.userService.getUserByEmail(email);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "User fetched successfully",
                            user
                            )
                    );
        }

        Page<UserModel> users = this.userService.getAllUsers(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(
                        HttpStatus.OK.value(),
                        "User list fetched successfully",
                        users
                        )
                );
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse<?>> createUser(
            @Valid @RequestBody CreateUserRequest createUserRequest
    ) {
        UserModel user = this.userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse<>(
                        HttpStatus.CREATED.value(),
                        "User created successfully",
                        user
                        )
                );
    }

    @PatchMapping("/update")
    public ResponseEntity<GenericResponse<?>> updateUserById(
            @RequestParam UUID userId,
            @Valid @RequestBody UpdateUserByIdRequest updateUserByIdRequest
    ) {
        UserModel user = this.userService.updateUserByUserId(userId, updateUserByIdRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(
                        HttpStatus.OK.value(),
                        "User updated successfully",
                        user
                        )
                );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<GenericResponse<?>> deleteUserById(
            @RequestParam UUID userId
    ) {
        this.userService.deleteUserByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User deleted successfully",
                                null
                        )
                );
    }
}
