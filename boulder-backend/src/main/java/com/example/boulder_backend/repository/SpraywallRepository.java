package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.Spraywall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpraywallRepository extends JpaRepository<Spraywall, UUID> {

    List<Spraywall> findByGymIdAndIsArchivedFalse(UUID gymId);
    List<Spraywall> findByGymIdAndIsArchivedTrue(UUID gymId);
    Optional<Spraywall> findByIdAndGym_Id(UUID id, UUID gymId);

    // Sichtbare (nicht archivierte) für einen User (öffentlich ODER vom User erstellt)
    @Query("""
        SELECT s FROM Spraywall s
        WHERE s.isArchived = false
          AND (s.isPublic = true OR s.createdBy.id = :userId)
        """)
    List<Spraywall> findVisibleNotArchived(@Param("userId") UUID userId);

    // Sichtbare (archiv-Flag steuerbar) pro Gym
    @Query("""
        SELECT s FROM Spraywall s
        WHERE s.gym.id = :gymId
          AND s.isArchived = :archived
          AND (s.isPublic = true OR s.createdBy.id = :userId)
        """)
    List<Spraywall> findVisibleByGymAndArchived(
            @Param("gymId") UUID gymId,
            @Param("userId") UUID userId,
            @Param("archived") boolean archived
    );
}
