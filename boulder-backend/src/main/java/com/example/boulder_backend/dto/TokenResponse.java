package com.example.boulder_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @AllArgsConstructor @NoArgsConstructor
public class TokenResponse {
    private String accessToken;
    private long accessTokenExpiresIn;   // Sekunden
    private String refreshToken;
    private long refreshTokenExpiresIn;  // Sekunden

}

