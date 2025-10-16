package com.example.boulder_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class SpraywallDto {
    private UUID id;

    private String name;
    private String description;
    private String photoUrl;

    // Erwartet "publicVisible" (wie vom Android-Client gesendet),
    // akzeptiert zusätzlich "isPublic" als Fallback.
    @JsonProperty("publicVisible")
    @JsonAlias({"isPublic"})
    private boolean publicVisible;

    @JsonProperty("archived")
    private boolean archived;

    private UUID gymId;
    private UUID createdBy;
}
