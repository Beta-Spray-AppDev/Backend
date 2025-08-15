package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Gym {

    @Id
    private UUID id;

    private String name;
    private String location;
    private String description;

    private UUID createdBy;

    private long createdAt;
    private long lastUpdated;
    private boolean isPublic;
    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    private List<Spraywall> spraywalls;
}
