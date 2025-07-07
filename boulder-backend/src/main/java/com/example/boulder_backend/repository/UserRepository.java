package com.example.boulder_backend.repository;

import com.example.boulder_backend.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {}
