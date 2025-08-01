package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GymRepository extends JpaRepository<Gym, UUID> {}
