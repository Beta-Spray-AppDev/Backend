package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.GymDto;
import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.service.AuthService;
import com.example.boulder_backend.service.GymService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gyms")
public class GymController {

    private final GymService gymService;
    private final AuthService authService;

    public GymController(GymService gymService, AuthService authService) {
        this.gymService = gymService;
        this.authService = authService;
    }

    /**
     *
     * POST und GET auf der SELBEN route!!!
     * POST /api/gyms
     * Fügt ein neues Gym hinzu.
     */
    @PostMapping
    public ResponseEntity<Gym> createGym(@RequestBody GymDto gymDto, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        Gym savedGym = gymService.createGym(gymDto, userId);
        return ResponseEntity.ok(savedGym);
    }

    /**
     * GET /api/gyms
     * Gibt eine Liste aller Gyms zurück.
     */
    @GetMapping
    public ResponseEntity<List<GymDto>> getAllGyms( @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        List<GymDto> gyms = gymService.getAllVisibleGyms(userId);
        return ResponseEntity.ok(gyms);
    }

}
