package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.BoulderDto;
import com.example.boulder_backend.dto.TickCreateRequest;
import com.example.boulder_backend.dto.TickDto;
import com.example.boulder_backend.model.Boulder;
import com.example.boulder_backend.model.BoulderTick;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.BoulderRepository;
import com.example.boulder_backend.repository.BoulderTickRepository;
import org.springframework.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BoulderTickService {
    private final BoulderTickRepository tickRepo;
    private final AuthService authService;
    private final BoulderRepository boulderRepository;
    private final BoulderService boulderService;


    // Methode zum Setzen eines Ticks
    public TickDto tick(UUID boulderId, UUID userId, @Nullable TickCreateRequest req) {

        Optional<BoulderTick> opt = tickRepo.findByBoulderIdAndUserId(boulderId, userId);
        // Falls Tick bereits existiert
        if (opt.isPresent()) {
            // gib existierenden Tick zurück -
            BoulderTick existing = opt.get();

            // Optional: wenn Request Werte mitbringt → partielles Update
            if (req != null) {
                if (req.getStars() != null) existing.setStars(validateStars(req.getStars()));
                if (req.getProposedGrade() != null) existing.setProposedGrade(trimOrNull(req.getProposedGrade()));
                tickRepo.save(existing);
            }

            return toDto(existing);

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


        if (req != null) {
            if (req.getStars() != null) t.setStars(validateStars(req.getStars()));
            t.setProposedGrade(trimOrNull(req.getProposedGrade()));
        }



        return toDto(tickRepo.save(t));
    }



    private static int validateStars(int s) {
        if (s < 1 || s > 5) throw new IllegalArgumentException("stars must be 1..5");
        return s;
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
        dto.setStars(t.getStars());
        dto.setProposedGrade(t.getProposedGrade());
        return dto;
    }
}
