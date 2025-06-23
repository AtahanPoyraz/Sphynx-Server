package io.sphynx.server.service;

import io.sphynx.server.dto.user.CreateUserRequest;
import io.sphynx.server.dto.user.UpdateUserByIdRequest;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel getUserByUserId(UUID userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    public UserModel getUserByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public Page<UserModel> getAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    public UserModel createUser(CreateUserRequest createUserRequest) {
        if (this.userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserModel user = new UserModel();
        user.setFirstName(createUserRequest.getFirstName());
        user.setLastName(createUserRequest.getLastName());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setIsEnabled(createUserRequest.getIsEnabled());
        user.setIsAccountNonExpired(createUserRequest.getIsAccountNonExpired());
        user.setIsAccountNonLocked(createUserRequest.getIsAccountNonLocked());
        user.setIsCredentialsNonExpired(createUserRequest.getIsCredentialsNonExpired());
        user.setRoles(createUserRequest.getRoles());
        user.setAccountType(createUserRequest.getAccountType());

        return this.userRepository.save(user);
    }

    public UserModel updateUserByUserId(UUID userId, UpdateUserByIdRequest updateUserByIdRequest) {
        UserModel user = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (updateUserByIdRequest.getFirstName() != null) {
            user.setFirstName(updateUserByIdRequest.getFirstName());
        }

        if (updateUserByIdRequest.getLastName() != null) {
            user.setLastName(updateUserByIdRequest.getLastName());
        }

        if (updateUserByIdRequest.getEmail() != null) {
            user.setEmail(updateUserByIdRequest.getEmail());
        }

        if (updateUserByIdRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserByIdRequest.getPassword()));
        }

        if (updateUserByIdRequest.getAccountType() != null) {
            user.setAccountType(updateUserByIdRequest.getAccountType());
        }

        if (updateUserByIdRequest.getIsEnabled() != null) {
            user.setIsEnabled(updateUserByIdRequest.getIsEnabled());
        }

        if (updateUserByIdRequest.getIsAccountNonExpired() != null) {
            user.setIsAccountNonExpired(updateUserByIdRequest.getIsAccountNonExpired());
        }

        if (updateUserByIdRequest.getIsAccountNonLocked() != null) {
            user.setIsAccountNonLocked(updateUserByIdRequest.getIsAccountNonLocked());
        }

        if (updateUserByIdRequest.getIsCredentialsNonExpired() != null) {
            user.setIsCredentialsNonExpired(updateUserByIdRequest.getIsCredentialsNonExpired());
        }

        if (updateUserByIdRequest.getRoles() != null) {
            user.setRoles(updateUserByIdRequest.getRoles());
        }

        return this.userRepository.save(user);
    }

    public void deleteUserByUserId(UUID userId) {
        if (!this.userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        this.userRepository.deleteById(userId);
    }
}
