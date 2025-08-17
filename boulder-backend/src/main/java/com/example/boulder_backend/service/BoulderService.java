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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoulderService {

    private final BoulderRepository boulderRepository;
    private final SpraywallRepository spraywallRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    //CREATE
    public BoulderDto createBoulder(BoulderDto dto, String authHeader){

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

        return dto;
    }
    // READ: meine Boulder
    public List<BoulderDto> getMyBoulders(String authHeader) {
        UUID userId = authService.extractUserId(authHeader); //

        return boulderRepository.findByCreatedById(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }


    //Mapper: Entity -> DTO
    public BoulderDto toDto(Boulder boulder) {
        BoulderDto dto = new BoulderDto();
        dto.setId(boulder.getId());
        dto.setName(boulder.getName());
        dto.setDifficulty(boulder.getDifficulty());
        dto.setSpraywallId(boulder.getSpraywall().getId());
        dto.setCreatedBy(boulder.getCreatedBy().getId());

        dto.setCreatedAt(boulder.getCreatedAt());
        dto.setLastUpdated(boulder.getLastUpdated());
        dto.setCreatedByUsername(boulder.getCreatedBy().getUsername());
        dto.setSpraywallImageUrl(boulder.getSpraywall().getPhotoUrl());
        dto.setSpraywallName(boulder.getSpraywall().getName());
        dto.setGymName(boulder.getSpraywall().getGym().getName());


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

    // READ: by spraywall
    public List<BoulderDto> getBouldersBySpraywall(UUID spraywallId) {
        List<Boulder> boulders = boulderRepository.findBySpraywallId(spraywallId);

        return boulders.stream()
                .map(this::toDto)
                .toList();
    }
    //READ: einzelner Boulder
    public BoulderDto getBoulderById(UUID boulderId) {
        Boulder boulder = boulderRepository.findById(boulderId)
                .orElseThrow(() -> new RuntimeException("Boulder nicht gefunden"));
        return toDto(boulder);
    }

    //UPADTE: Replace
    @Transactional
    public BoulderDto updateBoulder(UUID boulderId, BoulderDto dto, String authHeader) {
        UUID userId = authService.extractUserId(authHeader);

        Boulder boulder = boulderRepository.findById(boulderId)
                .orElseThrow(() -> new RuntimeException("Boulder nicht gefunden"));

        // Optional: Ownership check
        if (!boulder.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Keine Berechtigung zum Bearbeiten dieses Boulders");
        }

        // Spraywall setzen (falls änderbar)
        Spraywall spraywall = spraywallRepository.findById(dto.getSpraywallId())
                .orElseThrow(() -> new RuntimeException("Spraywall nicht gefunden"));

        boulder.setName(dto.getName());
        boulder.setDifficulty(dto.getDifficulty());
        boulder.setSpraywall(spraywall);
        boulder.setLastUpdated(System.currentTimeMillis());

        // Holds ersetzen (einfach & robust)
        boulder.getHolds().clear();
        boulder.getHolds().addAll(dtoToHolds(dto, boulder));

        Boulder saved = boulderRepository.save(boulder);
        return toDto(saved);
    }

    @Transactional
    public void deleteBoulder(UUID boulderId, String authHeader) {
        UUID userId = authService.extractUserId(authHeader);

        Boulder boulder = boulderRepository.findById(boulderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boulder nicht gefunden"));

        if (boulder.getCreatedBy() == null || !boulder.getCreatedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Keine Berechtigung");
        }

        try {
            boulderRepository.delete(boulder); // löscht Holds; Ticks bleiben, boulder_id -> NULL
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // tritt nur auf, wenn FK nicht nullable/ohne ON DELETE SET NULL ist
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Löschen nicht möglich: Bitte DB-Migration für ON DELETE SET NULL ausführen.",
                    ex
            );
        }
    }




    //Mapep: DTO -> Holds
    private List<Hold> dtoToHolds(BoulderDto dto, Boulder owner) {
        return dto.getHolds().stream().map(hd -> {
            Hold h = new Hold();
            h.setId(hd.getId());
            h.setX(hd.getX());
            h.setY(hd.getY());
            h.setType(hd.getType());
            h.setBoulder(owner);
            return h;
        }).toList();
    }




}
