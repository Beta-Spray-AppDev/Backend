package com.example.boulder_backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GymDto {
    private UUID id;
    private String name;
    private String location;
    private String description;
    private UUID createdBy;
    private long createdAt;
    private long lastUpdated;
    private List<SpraywallDto> spraywalls;
}
