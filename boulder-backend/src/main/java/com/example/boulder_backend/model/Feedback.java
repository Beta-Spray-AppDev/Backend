package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class Feedback {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name="UUID", strategy="org.hibernate.id.UUIDGenerator")
    @Column(updatable=false, nullable=false)
    private UUID id;

    @Column(nullable=false)
    private String username;      // nur Text

    @Column(nullable=false)
    private Integer stars;        // 1..5

    @Column(length=4000)
    private String message;

    @Column(nullable=false)
    private Long createdAt;       // epoch millis, wie bei dir üblich

    @Column(length=64)
    private String appVersion;

    @Column(length=128)
    private String deviceInfo;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now().toEpochMilli();
    }
}
