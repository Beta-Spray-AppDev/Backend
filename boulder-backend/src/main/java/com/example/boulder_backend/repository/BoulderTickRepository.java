package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.BoulderTick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoulderTickRepository extends JpaRepository<BoulderTick, UUID> {
    boolean existsByBoulderIdAndUserId(UUID boulderId, UUID userId);

    // Liefert alle Ticks eines Benutzers
    List<BoulderTick> findByUserId(UUID userId);

    // Liefert alle Ticks für einen Boulder
    List<BoulderTick> findByBoulderId(UUID boulderId);
}
