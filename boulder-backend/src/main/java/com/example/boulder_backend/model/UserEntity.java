package com.example.boulder_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Data
@Table(name = "app_user")
public class UserEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;


    @Column(unique = true, nullable = false)
    private String username;


    @Column(name = "username_norm", nullable = false, unique = true)  //case-insensitive
    private String usernameNorm;

    @Column(unique = true)
    private String email;


    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    private long createdAt;


}
