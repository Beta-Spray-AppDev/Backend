package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Hold {

    @Id
    private UUID id;

    private float x;
    private float y;
    private String type;

    @ManyToOne
    @JoinColumn(name = "boulder_id")
    private Boulder boulder;
}
