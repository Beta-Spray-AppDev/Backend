package com.example.boulder_backend.dto;

import lombok.Data;

@Data
public class CreateFeedbackDto {
    private Integer stars;
    private String message;
    private String username;
    private String appVersion;
    private String deviceInfo;
}
