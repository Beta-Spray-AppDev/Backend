package com.example.boulder_backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CommentDto {
    private UUID id;
    private String content;

    private long created;        // BIGINT millis

    private UUID boulderId;
    private UUID userId;

    private String createdByUsername;
}
