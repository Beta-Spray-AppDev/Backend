package com.example.boulder_backend.dto;

import lombok.Data;

    @Data //erzeugt automatisch Getter & Setter
    public class RegisterDto {
        private String username;
        private String email;
        private String password;
    }

