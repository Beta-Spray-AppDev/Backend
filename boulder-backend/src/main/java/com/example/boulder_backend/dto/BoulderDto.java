package com.example.boulder_backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BoulderDto {
    private UUID id;
    private String name;
    private String difficulty;
    private UUID spraywallId;
    private List<HoldDto> holds;

    private UUID createdBy;

    private Long createdAt;
    private Long lastUpdated;
    private String createdByUsername;
    private String spraywallImageUrl;
    private String spraywallName;
    private String gymName;
    private String setterNote;


    private Double avgStars;   // null, wenn keine Votes
    private Integer starsCount;




}
