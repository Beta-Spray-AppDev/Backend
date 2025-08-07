package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.BoulderDto;
import com.example.boulder_backend.dto.HoldDto;
import com.example.boulder_backend.model.Boulder;
import com.example.boulder_backend.model.Hold;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.BoulderRepository;
import com.example.boulder_backend.repository.SpraywallRepository;
import com.example.boulder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoulderService {

    private final BoulderRepository boulderRepository;
    private final SpraywallRepository spraywallRepository;
    private final UserRepository userRepository;
    private final AuthService authService;


    public void createBoulder(BoulderDto dto, String authHeader){

        UUID userId = authService.extractUserId(authHeader);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Spraywall spraywall = spraywallRepository.findById(dto.getSpraywallId())
                .orElseThrow(() -> new RuntimeException("Spraywall not found"));

        Boulder boulder = new Boulder();
        boulder.setId(UUID.randomUUID());
        boulder.setName(dto.getName());
        boulder.setDifficulty(dto.getDifficulty());
        boulder.setCreatedAt(System.currentTimeMillis());
        boulder.setLastUpdated(System.currentTimeMillis());
        boulder.setCreatedBy(user);
        boulder.setSpraywall(spraywall);

        List<Hold> holds = dto.getHolds().stream().map(holdDto -> {
            Hold hold = new Hold();
            hold.setId(holdDto.getId());
            hold.setX(holdDto.getX());
            hold.setY(holdDto.getY());
            hold.setType(holdDto.getType());
            hold.setBoulder(boulder);
            return hold;
        }).toList();

        boulder.setHolds(holds);
        boulderRepository.save(boulder);

    }

    public List<BoulderDto> getMyBoulders(String authHeader) {
        UUID userId = authService.extractUserId(authHeader); //

        return boulderRepository.findByCreatedById(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private BoulderDto toDto(Boulder boulder) {
        BoulderDto dto = new BoulderDto();
        dto.setName(boulder.getName());
        dto.setDifficulty(boulder.getDifficulty());
        dto.setSpraywallId(boulder.getSpraywall().getId());
        dto.setCreatedBy(boulder.getCreatedBy().getId());

        dto.setHolds(
                boulder.getHolds().stream().map(hold -> {
                    HoldDto holdDto = new HoldDto();
                    holdDto.setId(hold.getId());
                    holdDto.setX(hold.getX());
                    holdDto.setY(hold.getY());
                    holdDto.setType(hold.getType());
                    return holdDto;
                }).toList()
        );

        return dto;
    }


    public List<BoulderDto> getBouldersBySpraywall(UUID spraywallId) {
        List<Boulder> boulders = boulderRepository.findBySpraywallId(spraywallId);

        return boulders.stream()
                .map(this::toDto) // 👉 benutze schon bestehende Methode
                .toList();
    }




}
