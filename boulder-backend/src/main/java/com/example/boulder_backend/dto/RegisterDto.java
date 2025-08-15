package com.example.boulder_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

    @Data //erzeugt automatisch Getter & Setter
    public class RegisterDto {

        @NotBlank(message = "username_required")
        private String username;

        @NotBlank(message = "email_required")
        @Email(message = "email_invalid")
        private String email;

        @NotBlank(message = "password_required")
        private String password;
    }

