package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.service.AuthService;
import com.example.boulder_backend.service.SpraywallService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;




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
            @RequestBody SpraywallDto dto, @AuthenticationPrincipal Jwt jwt) {

        UUID currentUserId = UUID.fromString(jwt.getClaim("userId").toString());
        Spraywall saved = spraywallService.createSpraywall(dto, currentUserId);
        return ResponseEntity.ok(spraywallService.toDto(saved));
    }





    /**
     * GET /api/spraywalls
     * Listet alle Spraywalls
     */
    @GetMapping
    public ResponseEntity<List<SpraywallDto>> getAll(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        List<SpraywallDto> list = spraywallService.getAllVisible(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<SpraywallDto>> getSpraywallsByGym(
            @PathVariable UUID gymId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        List<SpraywallDto> spraywalls = spraywallService.getAllVisibleByGym(gymId, userId);
        return ResponseEntity.ok(spraywalls);
    }



    @GetMapping("/{id}")
    public ResponseEntity<SpraywallDto> getSpraywallById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        SpraywallDto dto = spraywallService.getVisibleById(id, userId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }






}
