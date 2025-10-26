package com.example.boulder_backend.controller;


import com.example.boulder_backend.dto.BoulderDto;
import com.example.boulder_backend.dto.TickCreateRequest;
import com.example.boulder_backend.dto.TickDto;
import com.example.boulder_backend.dto.TickWithBoulderDto;
import com.example.boulder_backend.service.BoulderService;
import com.example.boulder_backend.service.BoulderTickService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boulders")
@RequiredArgsConstructor
public class BoulderTickController {
    private final BoulderTickService boulderTickService;


    //Endpoint um Boulder zu ticken
    @PostMapping("/{boulderId}/ticks")
    public ResponseEntity<TickDto> tick(@PathVariable UUID boulderId, @AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) TickCreateRequest body) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        return ResponseEntity.ok(boulderTickService.tick(boulderId, userId, body));
    }

    // holt sich alle ticks des users
    @GetMapping("/ticks/mine")
    public List<TickWithBoulderDto> myTicks(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());

        return boulderTickService.getMyTickedBoulders(userId);
    }



    // Untick per Tick-ID
    @DeleteMapping("/ticks/{tickId}")
    public ResponseEntity<Void> untickByTickId(@PathVariable UUID tickId,
                                               @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        boulderTickService.untickByTickId(tickId, userId);
        return ResponseEntity.noContent().build();
    }



    // Untick einzelner Boulder
    @DeleteMapping("/{boulderId}/ticks")
    public ResponseEntity<Void> untick(@PathVariable UUID boulderId,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());

        boulderTickService.untick(boulderId, userId);
        return ResponseEntity.noContent().build(); // 204
    }


}
