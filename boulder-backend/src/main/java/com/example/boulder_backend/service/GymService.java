package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.GymDto;
import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.repository.GymRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GymService {

    private final GymRepository gymRepository;

    public GymService(GymRepository gymRepository) {
        this.gymRepository = gymRepository;
    }

    public Gym createGym(GymDto dto) {
        Gym gym = new Gym();
        gym.setId(UUID.randomUUID());
        gym.setName(dto.getName());
        gym.setLocation(dto.getLocation());
        gym.setDescription(dto.getDescription());
        gym.setCreatedAt(System.currentTimeMillis());
        gym.setLastUpdated(System.currentTimeMillis());

        // TEST: User-ID setzen (später aus Token)
        gym.setCreatedBy(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        return gymRepository.save(gym);
    }


    public List<GymDto> getAllGymsAsDto() {
        List<Gym> gyms = gymRepository.findAll();

        return gyms.stream().map(gym -> {
            GymDto dto = new GymDto();
            dto.setId(gym.getId());
            dto.setName(gym.getName());
            dto.setLocation(gym.getLocation());
            dto.setDescription(gym.getDescription());
            dto.setCreatedBy(gym.getCreatedBy());
            dto.setCreatedAt(gym.getCreatedAt());
            dto.setLastUpdated(gym.getLastUpdated());

            List<SpraywallDto> sprayDtos = gym.getSpraywalls().stream().map(s -> {
                SpraywallDto sDto = new SpraywallDto();
                sDto.setName(s.getName());
                sDto.setDescription(s.getDescription());
                sDto.setPhotoUrl(s.getPhotoUrl());
                sDto.setPublicVisible(s.isPublic());
                sDto.setGymId(gym.getId());
                return sDto;
            }).toList();

            dto.setSpraywalls(sprayDtos);

            return dto;
        }).toList();
    }


}
