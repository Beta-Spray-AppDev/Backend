package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Boulder {

    @Id
    private UUID id;

    private String name;
    private String difficulty;

    private long createdAt;
    private long lastUpdated;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @ManyToOne
    @JoinColumn(name = "spraywall_id")
    private Spraywall spraywall;

    @OneToMany(mappedBy = "boulder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Hold> holds;
}
