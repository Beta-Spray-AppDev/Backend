package com.example.boulder_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class SpraywallDto {
    private String name;
    private String description;
    private String photoUrl;
    @JsonProperty("isPublic")
    private boolean publicVisible;
    private UUID gymId;
    private UUID id;
    private UUID createdBy;


}
