package io.sphynx.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "email flag cannot be empty")
    private String email;

    @NotBlank(message = "password flag cannot be empty")
    private String password;
}
