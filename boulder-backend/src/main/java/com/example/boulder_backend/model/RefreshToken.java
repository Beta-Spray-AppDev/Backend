package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity @Data
public class RefreshToken {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    private UUID id;

    @Column(nullable=false, unique=true, length=255)
    private String tokenHash;           // Hash statt Klartext

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    private UserEntity user;

    @Column(nullable=false)
    private Instant issuedAt;

    @Column(nullable=false)
    private Instant expiresAt;

    @Column(nullable=false)
    private boolean revoked = false;

    @Column(length=64)
    private String deviceId; // optional (pro Gerät)
}
