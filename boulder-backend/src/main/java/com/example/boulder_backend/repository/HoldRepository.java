package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.Hold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HoldRepository extends JpaRepository<Hold, UUID> {
    List<Hold> findByBoulderId(UUID boulderId);
}
