package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.CreateFeedbackDto;
import com.example.boulder_backend.dto.FeedbackDto;
import com.example.boulder_backend.mapper.FeedbackMapper;
import com.example.boulder_backend.model.Feedback;
import com.example.boulder_backend.repository.FeedbackRepository;
import com.example.boulder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository repo;
    private final NotifyService notifyService;
    private final UserRepository userRepository; // für Username-lookup per userId

    public FeedbackDto create(CreateFeedbackDto body, UUID userId) {
        // Body -> Entity
        Feedback fb = FeedbackMapper.toEntity(body);

        // Username aus Token-User herleiten, falls im Body leer
        if ((fb.getUsername() == null || fb.getUsername().isBlank()) && userId != null) {
            userRepository.findById(userId).ifPresent(u -> fb.setUsername(u.getUsername()));
        }
        if (fb.getUsername() == null || fb.getUsername().isBlank()) {
            fb.setUsername("anonym");
        }

        // createdAt wird per @PrePersist gesetzt, aber doppelt ist ok (optional):
        if (fb.getCreatedAt() == null) {
            fb.setCreatedAt(System.currentTimeMillis());
        }

        Feedback saved = repo.save(fb);

        // asynchron benachrichtigen
        notifyService.onNewFeedback(saved);

        // Entity -> DTO
        return FeedbackMapper.toDto(saved);
    }

    public List<FeedbackDto> listAll() {
        return repo.findAll().stream()
                .map(FeedbackMapper::toDto)
                .toList();
    }
}
