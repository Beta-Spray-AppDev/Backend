package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.Boulder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoulderRepository extends JpaRepository<Boulder, UUID> {
    List<Boulder> findBySpraywallId(UUID spraywallId);
    List<Boulder> findByCreatedById(UUID userId);

}
