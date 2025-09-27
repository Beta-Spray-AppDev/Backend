package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.GymRepository;
import com.example.boulder_backend.repository.SpraywallRepository;
import com.example.boulder_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SpraywallService {

    private final SpraywallRepository spraywallRepository;
    private final GymRepository gymRepository;
    private final UserRepository userRepository;

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

        return spraywallRepository.save(spraywall);
    }

    public List<Spraywall> getAllSpraywalls() {
        return spraywallRepository.findAll();
    }

    public List<SpraywallDto> getAllVisible(UUID currentUserId) {
        return spraywallRepository.findByIsPublicTrueOrCreatedBy_Id(currentUserId)
                .stream()
                .map(this::toDto)
                .toList();
    }



    // nur private wenn user = createdby()
    public SpraywallDto getVisibleById(UUID id, UUID currentUserId) {
        return spraywallRepository.findById(id)
                .filter(s -> s.isPublic() ||
                        (s.getCreatedBy() != null && currentUserId.equals(s.getCreatedBy().getId())))
                .map(this::toDto)
                .orElse(null);
    }




    public List<SpraywallDto> getAllVisibleByGym(UUID gymId, UUID userId) {
        return spraywallRepository.findByGymId(gymId)        // hol nur dieses Gym
                .stream()
                .filter(s -> s.isPublic() ||
                        (s.getCreatedBy() != null && userId.equals(s.getCreatedBy().getId())))
                .map(this::toDto)
                .toList();
    }



    public List<SpraywallDto> getSpraywallsByGym(UUID gymId) {
        return spraywallRepository.findByGymId(gymId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public SpraywallDto toDto(Spraywall spraywall) {
        SpraywallDto dto = new SpraywallDto();
        dto.setId(spraywall.getId());
        dto.setName(spraywall.getName());
        dto.setDescription(spraywall.getDescription());
        dto.setPhotoUrl(spraywall.getPhotoUrl());
        dto.setPublicVisible(spraywall.isPublic());
        dto.setGymId(spraywall.getGym().getId());

        if (spraywall.getCreatedBy() != null) {               //  null-sicher für alte einträge
            dto.setCreatedBy(spraywall.getCreatedBy().getId());
        } else {
            dto.setCreatedBy(null);
        }
        return dto;
    }

}
