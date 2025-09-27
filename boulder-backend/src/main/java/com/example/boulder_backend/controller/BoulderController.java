package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.BoulderDto;
import com.example.boulder_backend.dto.HoldDto;
import com.example.boulder_backend.model.Boulder;
import com.example.boulder_backend.model.Hold;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.BoulderRepository;
import com.example.boulder_backend.repository.SpraywallRepository;
import com.example.boulder_backend.repository.UserRepository;
import com.example.boulder_backend.service.BoulderService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/boulders")
@RequiredArgsConstructor
public class BoulderController {

    private final BoulderRepository boulderRepository;
    private final SpraywallRepository spraywallRepository;
    private final UserRepository userRepository;
    private final BoulderService boulderService;


    @Value("${jwt.secret}")
    private String jwtSecret;

    // BoulderController.java
    @PostMapping
    public ResponseEntity<BoulderDto> createBoulder(
            @RequestBody BoulderDto dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        BoulderDto created = boulderService.createBoulder(dto, userId); // <- jetzt DTO zurück
        return ResponseEntity.ok(created);
    }

    // Holt sich alle Boulder vom eingeloggten User
    @GetMapping("/mine")
    public ResponseEntity<List<BoulderDto>> getMyBoulders(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        List<BoulderDto> boulders = boulderService.getMyBoulders(userId);
        return ResponseEntity.ok(boulders);
    }

    @GetMapping("/spraywall/{spraywallId}")
    public ResponseEntity<List<BoulderDto>> getBouldersBySpraywall(@PathVariable UUID spraywallId) {
        List<BoulderDto> result = boulderService.getBouldersBySpraywall(spraywallId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{boulderId}")
    public ResponseEntity<BoulderDto> getBoulderById(@PathVariable UUID boulderId) {
        return ResponseEntity.ok(boulderService.getBoulderById(boulderId));
    }

    @PutMapping("/{boulderId}")
    public ResponseEntity<BoulderDto> updateBoulder(@PathVariable UUID boulderId,
                                                    @RequestBody BoulderDto dto,
                                                    @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());

        return ResponseEntity.ok(boulderService.updateBoulder(boulderId, dto, userId));
    }

    @DeleteMapping("/{boulderId}")
    public ResponseEntity<Void> deleteBoulder(
            @PathVariable UUID boulderId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());

        boulderService.deleteBoulder(boulderId, userId);
        return ResponseEntity.noContent().build(); // 204
    }





}
