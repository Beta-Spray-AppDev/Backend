package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByBoulderIdOrderByCreatedAsc(UUID boulderId);
    List<Comment> findByUserIdOrderByCreatedDesc(UUID userId);
}
