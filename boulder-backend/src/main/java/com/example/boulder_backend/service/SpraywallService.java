package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.GymRepository;
import com.example.boulder_backend.repository.SpraywallRepository;
import com.example.boulder_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SpraywallService {

    private final SpraywallRepository spraywallRepository;
    private final GymRepository gymRepository;
    private final UserRepository userRepository;


    private static final Set<String> SUPERUSERS = Set.of("M_rkquez", "Eva", "Tiffy");

    private boolean isSuperUser(String username) {
        if (username == null) return false;
        for (String s : SUPERUSERS) {
            if (s.equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public SpraywallService(SpraywallRepository spraywallRepository,
                            GymRepository gymRepository,
                            UserRepository userRepository) {
        this.spraywallRepository = spraywallRepository;
        this.gymRepository = gymRepository;
        this.userRepository = userRepository;
    }

    public Spraywall createSpraywall(SpraywallDto dto, UUID currentUserId) {
        Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Gym not found"));

        Spraywall spraywall = new Spraywall();
        spraywall.setId(UUID.randomUUID());
        spraywall.setName(dto.getName());
        spraywall.setDescription(dto.getDescription());
        spraywall.setPhotoUrl(dto.getPhotoUrl());
        spraywall.setPublic(dto.isPublicVisible());
        spraywall.setCreatedAt(System.currentTimeMillis());
        spraywall.setLastUpdated(System.currentTimeMillis());
        spraywall.setGym(gym);


        UserEntity user = new UserEntity();
        user.setId(currentUserId);
        spraywall.setCreatedBy(user);
        spraywall.setArchived(false); // Safety
        return spraywallRepository.save(spraywall);
    }

    public List<Spraywall> getAllSpraywalls() {
        return spraywallRepository.findAll();
    }

    public List<SpraywallDto> getAllVisible(UUID currentUserId) {
        return spraywallRepository.findVisibleNotArchived(currentUserId)
                .stream().map(this::toDto).toList();
    }

    public SpraywallDto getVisibleById(UUID id, UUID currentUserId) {
        return spraywallRepository.findById(id)
                .filter(s -> !s.isArchived())
                .filter(s -> s.isPublic() ||
                        (s.getCreatedBy() != null && currentUserId.equals(s.getCreatedBy().getId())))
                .map(this::toDto)
                .orElse(null);
    }

    public List<SpraywallDto> getAllVisibleByGym(UUID gymId, UUID userId) {
        return spraywallRepository.findVisibleByGymAndArchived(gymId, userId, false)
                .stream().map(this::toDto).toList();
    }

    public List<SpraywallDto> getSpraywallsByGym(UUID gymId) {
        return spraywallRepository.findByGymIdAndIsArchivedFalse(gymId)
                .stream().map(this::toDto).toList();
    }

    public List<SpraywallDto> getArchivedVisibleByGym(UUID gymId, UUID userId) {
        return spraywallRepository.findVisibleByGymAndArchived(gymId, userId, true)
                .stream().map(this::toDto).toList();
    }

    // 🔒 Archiv-Toggle: Ersteller ODER Superuser
    public void setArchived(UUID spraywallId, boolean archived, UUID actingUserId) {
        Spraywall s = spraywallRepository.findById(spraywallId)
                .orElseThrow(() -> new RuntimeException("Spraywall not found"));

        UserEntity actor = userRepository.findById(actingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isCreator = s.getCreatedBy() != null && s.getCreatedBy().getId().equals(actor.getId());
        boolean isSuper   = isSuperUser(actor.getUsername());

        if (!isCreator && !isSuper) {
            // schönerer Statuscode für’s Frontend
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to archive");
        }

        s.setArchived(archived);
        s.setLastUpdated(System.currentTimeMillis());
        spraywallRepository.save(s);
    }

    public SpraywallDto toDto(Spraywall spraywall) {
        SpraywallDto dto = new SpraywallDto();
        dto.setId(spraywall.getId());
        dto.setName(spraywall.getName());
        dto.setDescription(spraywall.getDescription());
        dto.setPhotoUrl(spraywall.getPhotoUrl());
        dto.setPublicVisible(spraywall.isPublic());
        dto.setGymId(spraywall.getGym().getId());
        dto.setArchived(spraywall.isArchived());
        dto.setCreatedBy(spraywall.getCreatedBy() != null ? spraywall.getCreatedBy().getId() : null);
        return dto;
    }
}
