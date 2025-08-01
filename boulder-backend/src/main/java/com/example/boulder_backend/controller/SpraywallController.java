package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.service.SpraywallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/spraywalls")
public class SpraywallController {

    private final SpraywallService spraywallService;

    public SpraywallController(SpraywallService spraywallService) {
        this.spraywallService = spraywallService;
    }

    /**
     * POST /api/spraywalls
     * Erstellt eine neue Spraywall
     */
    @PostMapping
    public ResponseEntity<SpraywallDto> createSpraywall(@RequestBody SpraywallDto dto) {
        Spraywall saved = spraywallService.createSpraywall(dto);
        return ResponseEntity.ok(spraywallService.toDto(saved));
    }


    /**
     * GET /api/spraywalls
     * Listet alle Spraywalls
     */
    @GetMapping
    public ResponseEntity<List<SpraywallDto>> getAll() {
        List<SpraywallDto> all = spraywallService.getAllSpraywalls()
                .stream()
                .map(spraywallService::toDto)
                .toList();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<SpraywallDto>> getSpraywallsByGym(@PathVariable UUID gymId) {
        List<SpraywallDto> spraywalls = spraywallService.getSpraywallsByGym(gymId);
        return ResponseEntity.ok(spraywalls);
    }


}
