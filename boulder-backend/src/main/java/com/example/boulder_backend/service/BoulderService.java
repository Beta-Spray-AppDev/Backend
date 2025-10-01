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
import org.springframework.security.oauth2.jwt.Jwt;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoulderService {

    private final BoulderRepository boulderRepository;
    private final SpraywallRepository spraywallRepository;
    private final UserRepository userRepository;
    private final SuperuserRegistry superusers;

    // CREATE
    public BoulderDto createBoulder(BoulderDto dto, UUID userId) {

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
        boulder.setSetterNote(dto.getSetterNote()); // <- Note übernehmen

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

        Boulder saved = boulderRepository.save(boulder);
        return toDto(saved); // <- wichtig: das gespeicherte Objekt mappen & zurückgeben
    }

    // READ: meine Boulder
    public List<BoulderDto> getMyBoulders(UUID userId) {
        return boulderRepository.findByCreatedById(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Mapper: Entity -> DTO
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

        dto.setSetterNote(boulder.getSetterNote()); // <- FIX: DTO aus Entity befüllen (nicht umgekehrt!)

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
        return boulderRepository.findBySpraywallId(spraywallId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // READ: einzelner Boulder
    public BoulderDto getBoulderById(UUID boulderId) {
        Boulder boulder = boulderRepository.findById(boulderId)
                .orElseThrow(() -> new RuntimeException("Boulder nicht gefunden"));
        return toDto(boulder);
    }

    // UPDATE
    @Transactional
    public BoulderDto updateBoulder(UUID boulderId, BoulderDto dto, UUID userId) {

        Boulder boulder = boulderRepository.findById(boulderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boulder nicht gefunden"));

        UUID ownerId = Optional.ofNullable(boulder.getCreatedBy())
                .map(UserEntity::getId)
                .orElse(null);

        boolean allowed = Objects.equals(ownerId, userId) || superusers.isSuperuser(userId);
        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Nur der Ersteller oder ein Superuser darf diesen Boulder bearbeiten."
            );
        }

        Spraywall spraywall = spraywallRepository.findById(dto.getSpraywallId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spraywall nicht gefunden"));

        boulder.setName(dto.getName());
        boulder.setDifficulty(dto.getDifficulty());
        boulder.setSpraywall(spraywall);
        boulder.setLastUpdated(System.currentTimeMillis());
        boulder.setSetterNote(dto.getSetterNote()); // <- Note aktualisieren

        boulder.getHolds().clear();
        boulder.getHolds().addAll(dtoToHolds(dto, boulder));

        Boulder saved = boulderRepository.save(boulder);
        return toDto(saved);
    }

    @Transactional
    public void deleteBoulder(UUID boulderId, UUID userId) {
        Boulder boulder = boulderRepository.findById(boulderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boulder nicht gefunden"));

        boolean isOwner = Optional.ofNullable(boulder.getCreatedBy())
                .map(UserEntity::getId)
                .map(userId::equals)
                .orElse(false);

        boolean allowed = isOwner || superusers.isSuperuser(userId);
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Keine Berechtigung");

        try {
            boulderRepository.delete(boulder);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Löschen nicht möglich: Bitte DB-Migration für ON DELETE SET NULL ausführen.",
                    ex
            );
        }
    }

    // DTO -> Holds
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

