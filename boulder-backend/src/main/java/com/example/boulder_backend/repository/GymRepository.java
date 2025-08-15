package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.model.Spraywall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GymRepository extends JpaRepository<Gym, UUID> {


    List<Gym> findByIsPublicTrueOrCreatedBy(UUID createdBy);
}
