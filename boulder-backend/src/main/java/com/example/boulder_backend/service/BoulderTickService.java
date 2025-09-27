package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.BoulderDto;
import com.example.boulder_backend.dto.TickDto;
import com.example.boulder_backend.model.Boulder;
import com.example.boulder_backend.model.BoulderTick;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.BoulderRepository;
import com.example.boulder_backend.repository.BoulderTickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BoulderTickService {
    private final BoulderTickRepository tickRepo;
    private final AuthService authService;
    private final BoulderRepository boulderRepository;
    private final BoulderService boulderService;


    // Methode zum Setzen eines Ticks
    public TickDto tick(UUID boulderId, UUID userId) {

        // Falls Tick bereits existiert
        if (tickRepo.existsByBoulderIdAndUserId(boulderId, userId)) {
            // gib existierenden Tick zurück -
            return tickRepo.findByUserId(userId).stream()
                    .filter(t -> t.getBoulder().getId().equals(boulderId))
                    .findFirst().map(this::toDto).orElseThrow();
        }

        // Boulder aus DB laden
        Boulder b = boulderRepository.findById(boulderId)
                .orElseThrow(() -> new RuntimeException("Boulder nicht gefunden"));

        // Neuen Tick erstellen
        BoulderTick t = new BoulderTick();
        t.setId(UUID.randomUUID());
        t.setBoulder(b);
        UserEntity u = new UserEntity(); u.setId(userId); t.setUser(u);
        t.setCreatedAt(System.currentTimeMillis());

        return toDto(tickRepo.save(t));
    }

    @Transactional(readOnly = true)
    public List<BoulderDto> getMyTickedBoulders(UUID userId) {
        return tickRepo.findByUserIdAndBoulderIsNotNull(userId).stream()
                .map(BoulderTick::getBoulder)
                .map(boulderService::toDto)
                .toList();
    }


    @Transactional
    public void untick(UUID boulderId, UUID userId) {
        tickRepo.deleteByBoulderIdAndUserId(boulderId, userId);
    }




    private TickDto toDto(BoulderTick t) {
        TickDto dto = new TickDto();
        dto.setId(t.getId());
        dto.setBoulderId(t.getBoulder().getId());
        dto.setUserId(t.getUser().getId());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }
}
