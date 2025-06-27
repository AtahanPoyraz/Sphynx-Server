package io.sphynx.server.controller;

import io.sphynx.server.dto.GenericResponse;
import io.sphynx.server.dto.user.CreateUserRequest;
import io.sphynx.server.dto.user.UpdateUserByIdRequest;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.TokenType;
import io.sphynx.server.service.JwtService;
import io.sphynx.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
    public ResponseEntity<GenericResponse<?>> me(
            HttpServletRequest servletRequest
    ) {
        try {
            String authHeader = servletRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new GenericResponse<>(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Authorization header is missing or invalid",
                                null
                        ));
            }

            String token = authHeader.substring(7);
            UserModel user = this.jwtService.extractUserFromToken(token, TokenType.AUTH);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "User fetched successfully",
                            user
                            )
                    );

        } catch (Exception e) {
            logger.error("An error occurred while fetching the user:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "An error occurred while fetching the user: " + e.getMessage(),
                                    null
                            )
                    );
        }
    }

    @GetMapping("/get")
    public ResponseEntity<GenericResponse<?>> getUser(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String email,
            @ParameterObject Pageable pageable
    ) {
        try {
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

        } catch (Exception e) {
            logger.error("An error occurred while fetching the users:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while fetching the users: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse<?>> createUser(
            @Valid @RequestBody CreateUserRequest createUserRequest
    ) {
        try {
            UserModel user = this.userService.createUser(createUserRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new GenericResponse<>(
                            HttpStatus.CREATED.value(),
                            "User created successfully",
                            user
                            )
                    );

        } catch (Exception e) {
            logger.error("An error occurred while creating the user:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while creating the user: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<GenericResponse<?>> updateUserById(
            @RequestParam UUID userId,
            @Valid @RequestBody UpdateUserByIdRequest updateUserByIdRequest
    ) {
        try {
            UserModel user = this.userService.updateUserByUserId(userId, updateUserByIdRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "User updated successfully",
                            user
                            )
                    );

        } catch (Exception e) {
            logger.error("An error occurred while updating the user:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while updating the user: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<GenericResponse<?>> deleteUserById(
            @RequestParam UUID userId
    ) {
        try {
            this.userService.deleteUserByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "User deleted successfully",
                            null
                            )
                    );

        } catch (Exception e) {
            logger.error("An error occurred while deleting the user:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while deleting the user: " + e.getMessage(),
                            null
                            )
                    );
        }
    }
}
