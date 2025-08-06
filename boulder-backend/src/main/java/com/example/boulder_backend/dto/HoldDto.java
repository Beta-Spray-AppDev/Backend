package com.example.boulder_backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class HoldDto {

    private UUID id;
    private float x;
    private float y;
    private String type;

}
