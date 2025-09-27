package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.GymDto;
import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.repository.GymRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GymService {

    private final GymRepository gymRepository;

    public GymService(GymRepository gymRepository) {
        this.gymRepository = gymRepository;
    }

    public Gym createGym(GymDto dto, UUID userId) {
        Gym gym = new Gym();
        gym.setId(UUID.randomUUID());
        gym.setName(dto.getName());
        gym.setLocation(dto.getLocation());
        gym.setDescription(dto.getDescription());
        gym.setCreatedAt(System.currentTimeMillis());
        gym.setLastUpdated(System.currentTimeMillis());
        gym.setCreatedBy(userId);
        gym.setPublic(dto.isPublicVisible());

        return gymRepository.save(gym);
    }

    @Transactional(readOnly = true)
    public List<GymDto> getAllGymsAsDto() {
        return gymRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GymDto> getAllVisibleGyms(UUID userId) {
        return gymRepository.findByIsPublicTrueOrCreatedBy(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }


    private GymDto toDto(Gym gym) {
        if (gym == null) return null;

        GymDto dto = new GymDto();
        dto.setId(gym.getId());
        dto.setName(gym.getName());
        dto.setLocation(gym.getLocation());
        dto.setDescription(gym.getDescription());
        dto.setCreatedBy(gym.getCreatedBy());
        dto.setCreatedAt(gym.getCreatedAt());
        dto.setLastUpdated(gym.getLastUpdated());
        dto.setPublicVisible(gym.isPublic());

        // Spraywalls null-sicher mappen
        List<SpraywallDto> sprayDtos = gym.getSpraywalls() == null ? List.of()
                : gym.getSpraywalls().stream()
                .filter(Objects::nonNull)
                .map(s -> toDto(s, gym.getId()))
                .toList();

        dto.setSpraywalls(sprayDtos);
        return dto;
    }

    private SpraywallDto toDto(Spraywall s, UUID gymId) {
        SpraywallDto d = new SpraywallDto();

        d.setId(s.getId());
        d.setName(s.getName());
        d.setDescription(s.getDescription());
        d.setPhotoUrl(s.getPhotoUrl());
        d.setPublicVisible(s.isPublic());
        d.setGymId(gymId);
        return d;
    }
}
