package com.example.boulder_backend.dto;


import lombok.Data;

@Data
public class TickWithBoulderDto {
    private TickDto tick;        // enthält proposedGrade, stars, createdAt, boulderId, userId
    private BoulderDto boulder;  // kompletter Boulder
}
