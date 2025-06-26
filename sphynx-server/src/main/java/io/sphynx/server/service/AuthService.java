package io.sphynx.server.service;

import io.sphynx.server.dto.auth.ResetPasswordRequest;
import io.sphynx.server.dto.auth.SignInRequest;
import io.sphynx.server.dto.auth.SignUpRequest;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.AccountType;
import io.sphynx.server.model.enums.UserRole;
import io.sphynx.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel authenticate(SignInRequest signInRequest) {
        return this.userRepository.findByEmail(signInRequest.getEmail())
                .filter(user -> this.passwordEncoder.matches(signInRequest.getPassword(), user.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    }

    public UserModel register(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        UserModel user = new UserModel();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(this.passwordEncoder.encode(signUpRequest.getPassword()));
        user.setAccountType(AccountType.STANDARD);
        user.setIsEnabled(true);
        user.setIsAccountNonExpired(true);
        user.setIsAccountNonLocked(true);
        user.setIsCredentialsNonExpired(true);
        user.setRoles(EnumSet.of(UserRole.ROLE_USER));

        return this.userRepository.save(user);
    }

    public void resetPassword(UserModel user, ResetPasswordRequest resetPasswordRequest) {
        user.setPassword(this.passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        this.userRepository.save(user);
    }
}
