package com.example.boulder_backend.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class TickDto {

    private UUID id;
    private UUID boulderId;
    private UUID userId;
    private String style; //evtl später für flash usw
    private Long createdAt;
    private Integer stars;
    private String proposedGrade;
}

