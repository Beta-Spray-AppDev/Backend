package com.example.boulder_backend.repository;

import com.example.boulder_backend.dto.projection.BoulderRatingAggregate;
import com.example.boulder_backend.model.BoulderTick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoulderTickRepository extends JpaRepository<BoulderTick, UUID> {
    boolean existsByBoulderIdAndUserId(UUID boulderId, UUID userId);

    // Liefert alle Ticks eines Benutzers
    List<BoulderTick> findByUserId(UUID userId);

    // liefert nur Ticks, deren Boulder noch existiert
    List<BoulderTick> findByUserIdAndBoulderIsNotNull(UUID userId);


    // Liefert alle Ticks für einen Boulder
    List<BoulderTick> findByBoulderId(UUID boulderId);

    Optional<BoulderTick> findByBoulderIdAndUserId(UUID boulderId, UUID userId);



    void deleteByBoulderIdAndUserId(UUID boulderId, UUID userId);



    @Query("""
    select t from BoulderTick t
    join fetch t.boulder b
    where t.user.id = :userId and t.boulder is not null
""")
    List<BoulderTick> findByUserIdWithBoulder(UUID userId);



    @Query("""
    select t.boulder.id as boulderId,
           avg(t.stars)     as avgStars,
           count(t.stars)   as count
    from BoulderTick t
    where t.stars is not null
    group by t.boulder.id
""")
    List<BoulderRatingAggregate> findAllBoulderRatings();



    @Query("""
    select t.boulder.id as boulderId,
           avg(t.stars)     as avgStars,
           count(t.stars)   as count
    from BoulderTick t
    where t.stars is not null
      and t.boulder.spraywall.id = :spraywallId
    group by t.boulder.id
""")
    List<BoulderRatingAggregate> findRatingsBySpraywall(UUID spraywallId);

}
