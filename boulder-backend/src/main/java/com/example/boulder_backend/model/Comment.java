package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "comment") // passt zu deiner DB-Tabelle
@Data
public class Comment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private long created;                // BIGINT (Millis)

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @ManyToOne(optional = false)
    @JoinColumn(name = "boulder_id")    // FK -> boulder.id
    private Boulder boulder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")       // FK -> app_user.id (UserEntity)
    private UserEntity user;
}
