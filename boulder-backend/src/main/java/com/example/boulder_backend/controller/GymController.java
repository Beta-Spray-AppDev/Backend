package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.GymDto;
import com.example.boulder_backend.model.Gym;
import com.example.boulder_backend.service.GymService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gyms")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    /**
     *
     * POST und GET auf der SELBEN route!!!
     * POST /api/gyms
     * Fügt ein neues Gym hinzu.
     */
    @PostMapping
    public ResponseEntity<Gym> createGym(@RequestBody GymDto gymDto) {
        Gym savedGym = gymService.createGym(gymDto);
        return ResponseEntity.ok(savedGym);
    }

    /**
     * GET /api/gyms
     * Gibt eine Liste aller Gyms zurück.
     */
    @GetMapping
    public ResponseEntity<List<GymDto>> getAllGyms() {
        List<GymDto> gyms = gymService.getAllGymsAsDto();
        return ResponseEntity.ok(gyms);
    }

}
