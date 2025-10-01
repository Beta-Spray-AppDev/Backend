package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.CommentDto;
import com.example.boulder_backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments") // oder "/comments" – muss zu deinem Retrofit passen
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@RequestBody CommentDto dto,
                             @AuthenticationPrincipal Jwt jwt) {

        // exakt wie in BoulderController:
        Object claim = jwt.getClaim("userId");            // erwartet UUID als String
        if (claim == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId-Claim fehlt");
        }

        final UUID userId;
        try {
            userId = UUID.fromString(claim.toString());
        } catch (IllegalArgumentException ex) {
            // statt 500 sauber 401/400 liefern
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Ungültiger userId-Claim (keine UUID): " + claim
            );
        }

        return commentService.create(dto, userId);
    }

    @GetMapping("/boulder/{boulderId}")
    public List<CommentDto> getByBoulder(@PathVariable UUID boulderId) {
        return commentService.getByBoulder(boulderId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID commentId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        commentService.delete(commentId, userId);
    }
}

