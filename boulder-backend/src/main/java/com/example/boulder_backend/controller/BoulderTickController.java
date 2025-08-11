package com.example.boulder_backend.controller;


import com.example.boulder_backend.dto.TickDto;
import com.example.boulder_backend.service.BoulderService;
import com.example.boulder_backend.service.BoulderTickService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/boulders")
@RequiredArgsConstructor
public class BoulderTickController {
    private final BoulderTickService boulderTickService;


    //Endpoint um Boulder zu ticken
    @PostMapping("/{boulderId}/ticks")
    public ResponseEntity<TickDto> tick(@PathVariable UUID boulderId, @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(boulderTickService.tick(boulderId, auth));
    }
}
