package com.example.boulder_backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SpraywallDto {
    private String name;
    private String description;
    private String photoUrl;
    private boolean isPublic;
    private UUID gymId;
}
