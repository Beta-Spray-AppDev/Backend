package com.example.boulder_backend.dto;

import lombok.Data;

@Data
public class UpdateProfileDto {
    private String username;
    private String email;
    private String password;
}
