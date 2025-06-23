package io.sphynx.server.dto.user;

import io.sphynx.server.model.enums.AccountType;
import io.sphynx.server.model.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "firstName flag cannot be empty")
    private String firstName;

    @NotBlank(message = "lastName flag cannot be empty")
    private String lastName;

    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "email flag cannot be empty")
    private String email;

    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{6,20}$",
            message = "Password must contain at least one letter, one number, and one special character"
    )
    @NotBlank(message = "password flag cannot be empty")
    private String password;

    @NotNull(message = "accountType flag cannot be null")
    private AccountType accountType;

    @NotNull(message = "isEnable flag cannot be null")
    private Boolean isEnabled;

    @NotNull(message = "isAccountNonExpired flag cannot be null")
    private Boolean isAccountNonExpired;

    @NotNull(message = "isAccountNonLocked flag cannot be null")
    private Boolean isAccountNonLocked;

    @NotNull(message = "isCredentialsNonExpired flag cannot be null")
    private Boolean isCredentialsNonExpired;

    @NotNull(message = "roles flag cannot be null")
    private Set<UserRole> roles;

}
