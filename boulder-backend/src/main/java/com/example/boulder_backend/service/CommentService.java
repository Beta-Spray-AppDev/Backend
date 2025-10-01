package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.CommentDto;
import com.example.boulder_backend.model.Boulder;
import com.example.boulder_backend.model.Comment;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.BoulderRepository;
import com.example.boulder_backend.repository.CommentRepository;
import com.example.boulder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoulderRepository boulderRepository;
    private final UserRepository userRepository;
    private final SuperuserRegistry superusers;

    // CREATE
    public CommentDto create(CommentDto dto, UUID userId) {
        if (dto.getBoulderId() == null || dto.getContent() == null || dto.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boulderId und content sind erforderlich");
        }

        Boulder boulder = boulderRepository.findById(dto.getBoulderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boulder nicht gefunden"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User nicht gefunden"));

        Comment c = new Comment();
        c.setId(UUID.randomUUID());
        c.setCreated(System.currentTimeMillis());
        c.setContent(dto.getContent());
        c.setBoulder(boulder);
        c.setUser(user);

        Comment saved = commentRepository.save(c);
        return toDto(saved);
    }

    // READ: alle Kommentare zu einem Boulder
    public List<CommentDto> getByBoulder(UUID boulderId) {
        return commentRepository.findByBoulderIdOrderByCreatedAsc(boulderId)
                .stream().map(this::toDto).toList();
    }

    // READ: meine Kommentare (optional)
    public List<CommentDto> getMine(UUID userId) {
        return commentRepository.findByUserIdOrderByCreatedDesc(userId)
                .stream().map(this::toDto).toList();
    }

    // DELETE: nur Ersteller oder Superuser
    public void delete(UUID commentId, UUID userId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kommentar nicht gefunden"));

        boolean isOwner = Objects.equals(c.getUser().getId(), userId);
        if (!isOwner && !superusers.isSuperuser(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Keine Berechtigung");
        }

        commentRepository.delete(c);
    }

    // Mapper
    private CommentDto toDto(Comment c) {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setContent(c.getContent());
        dto.setCreated(c.getCreated());
        dto.setBoulderId(c.getBoulder().getId());
        dto.setUserId(c.getUser().getId());
        dto.setCreatedByUsername(c.getUser().getUsername());
        return dto;
    }
}
