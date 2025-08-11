package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "boulder_tick", uniqueConstraints=@UniqueConstraint(columnNames={"boulder_id","user_id"}))
@Data
public class BoulderTick {
    @Id private UUID id;

    @ManyToOne @JoinColumn(name="boulder_id") private Boulder boulder;
    @ManyToOne
    @JoinColumn(name="user_id")    private UserEntity user;

    private long createdAt;
}
