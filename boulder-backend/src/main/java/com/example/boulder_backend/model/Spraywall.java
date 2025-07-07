package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Spraywall {

    @Id
    private UUID id;

    private String name;
    private String description;
    private String photoUrl;

    private boolean isPublic;

    private long createdAt;
    private long lastUpdated;

    @ManyToOne
    @JoinColumn(name = "gym_id")
    private Gym gym;
}
