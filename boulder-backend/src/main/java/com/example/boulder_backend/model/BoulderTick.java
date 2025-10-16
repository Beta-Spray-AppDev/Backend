package com.example.boulder_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "boulder_tick", uniqueConstraints=@UniqueConstraint(columnNames={"boulder_id","user_id"}))
@Data
public class BoulderTick {
    @Id private UUID id;

    @ManyToOne(optional = true)
    @JoinColumn(name="boulder_id", nullable = true)
    private Boulder boulder;


    @ManyToOne
    @JoinColumn(name="user_id")    private UserEntity user;

    private long createdAt;

    private Integer stars;              // 1..5, optional
    @Column(name="proposed_grade", length = 16)
    private String proposedGrade;


    @Column(name = "boulder_name_snapshot", length = 255)
    private String boulderNameSnapshot;
}
