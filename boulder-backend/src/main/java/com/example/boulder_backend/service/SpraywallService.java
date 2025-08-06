package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.repository.GymRepository;
import com.example.boulder_backend.repository.SpraywallRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SpraywallService {

    private final SpraywallRepository spraywallRepository;
    private final GymRepository gymRepository;

    public SpraywallService(SpraywallRepository spraywallRepository, GymRepository gymRepository) {
        this.spraywallRepository = spraywallRepository;
        this.gymRepository = gymRepository;
    }

    public Spraywall createSpraywall(SpraywallDto dto) {
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

        return spraywallRepository.save(spraywall);
    }


    public List<Spraywall> getAllSpraywalls() {
        return spraywallRepository.findAll();
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
        return dto;
    }


}
