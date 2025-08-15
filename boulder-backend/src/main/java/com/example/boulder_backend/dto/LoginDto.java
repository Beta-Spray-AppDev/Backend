package com.example.boulder_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "username_required")
    private String username;

    @NotBlank(message = "password_required")
    private String password;
}
