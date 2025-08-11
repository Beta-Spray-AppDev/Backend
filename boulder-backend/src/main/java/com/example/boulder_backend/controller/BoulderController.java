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
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<Void> createBoulder(@RequestBody BoulderDto dto, @RequestHeader("Authorization") String authHeader) {
        boulderService.createBoulder(dto, authHeader);
        return ResponseEntity.ok().build();
    }

    // Holt sich alle Boulder vom eingeloggten User
    @GetMapping("/mine")
    public ResponseEntity<List<BoulderDto>> getMyBoulders(
            @RequestHeader("Authorization") String authHeader
    ) {
        List<BoulderDto> boulders = boulderService.getMyBoulders(authHeader);
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
                                                    @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(boulderService.updateBoulder(boulderId, dto, authHeader));
    }

}
