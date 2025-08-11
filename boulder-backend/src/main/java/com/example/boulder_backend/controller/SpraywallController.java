package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.service.AuthService;
import com.example.boulder_backend.service.SpraywallService;
import io.jsonwebtoken.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/spraywalls")
public class SpraywallController {

    private final SpraywallService spraywallService;
    private final AuthService authService;

    public SpraywallController(SpraywallService spraywallService, AuthService authService) {
        this.spraywallService = spraywallService;
        this.authService = authService;
    }

    /**
     * POST /api/spraywalls
     * Erstellt eine neue Spraywall
     */
    @PostMapping
    public ResponseEntity<SpraywallDto> createSpraywall(
            @RequestBody SpraywallDto dto,
            @RequestHeader("Authorization") String authHeader) {

        UUID currentUserId = authService.extractUserId(authHeader);
        Spraywall saved = spraywallService.createSpraywall(dto, currentUserId);
        return ResponseEntity.ok(spraywallService.toDto(saved));
    }





    /**
     * GET /api/spraywalls
     * Listet alle Spraywalls
     */
    @GetMapping
    public ResponseEntity<List<SpraywallDto>> getAll(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authService.extractUserId(authHeader);
        List<SpraywallDto> list = spraywallService.getAllVisible(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<SpraywallDto>> getSpraywallsByGym(
            @PathVariable UUID gymId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authService.extractUserId(authHeader);
        List<SpraywallDto> spraywalls = spraywallService.getAllVisibleByGym(gymId, userId);
        return ResponseEntity.ok(spraywalls);
    }



}
