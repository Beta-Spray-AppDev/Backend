package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.CreateFeedbackDto;
import com.example.boulder_backend.dto.FeedbackDto;
import com.example.boulder_backend.service.AuthService;
import com.example.boulder_backend.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService; // neu
    private final AuthService authService;         // bleibt unverändert

    @PostMapping
    public ResponseEntity<FeedbackDto> create(
            @RequestBody CreateFeedbackDto body,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        UUID userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                userId = authService.extractUserId(authHeader);
            } catch (Exception ignored) {
                userId = null;
            }
        }

        FeedbackDto dto = feedbackService.create(body, userId);
        return ResponseEntity.created(URI.create("/api/feedback/" + dto.getId())).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackDto>> list() {
        return ResponseEntity.ok(feedbackService.listAll());
    }
}
