package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.apache.catalina.User;

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

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private UserEntity createdBy;
    private boolean isArchived = false;


    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = this.createdAt;
    }
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = System.currentTimeMillis();
    }


}
