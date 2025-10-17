package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(
        name = "password_reset_token",
        indexes = {
                @Index(columnList = "token_hash"),
                @Index(columnList = "user_id")
        }
)
@Data
public class PasswordResetToken {

    @Id
    private UUID id;                 // wie bei dir: ohne @GeneratedValue, erzeugen wir im Service

    @Column(name = "user_id", nullable = false)
    private UUID userId;             // FK auf UserEntity.id (UUID)

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;        // sha-256 hex

    @Column(name = "expires", nullable = false)
    private long expires;            //Millis seit Epoch (wie bei created)

    @Column(name = "used_at")
    private Long usedAt;             //Millis oder null

    @Column(name = "requested_ip")
    private String requestedIp;

    @Column(name = "requested_user_agent")
    private String requestedUserAgent;

    @Column(name = "created", nullable = false)
    private long created;            //Millis
}
