package com.example.boulder_backend.dto;


import lombok.Data;

@Data
public class TickCreateRequest {
    private Integer stars;         // optional, 1..5
    private String proposedGrade;  // optional, z.B. "6B+"
}
