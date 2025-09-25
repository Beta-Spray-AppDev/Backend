package com.example.boulder_backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class FeedbackDto {
    private String id;
    private String username;
    private Integer stars;
    private String message;
    private Long createdAt;
    private String appVersion;
    private String deviceInfo;
}
