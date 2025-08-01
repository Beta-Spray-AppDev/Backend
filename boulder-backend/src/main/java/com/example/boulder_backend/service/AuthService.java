package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.LoginDto;
import com.example.boulder_backend.dto.RegisterDto;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.UserRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor //erstellt automatsich Konstruktor mit final-Feldern
public class AuthService {
    private final UserRepository userRepository; //Zugriff auf UserDb

    public UserEntity register(RegisterDto request) {

        //neues UserEntity-Objekt erstellen
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        //pw hashen
        user.setPasswordHash(
                BCrypt.hashpw(request.getPassword(), BCrypt.gensalt())
        );
        user.setCreatedAt(System.currentTimeMillis());

        //user in db schreiben
        return userRepository.save(user);
    }

    public boolean login(LoginDto request) {
        // User mit passendem Namen suchen
        Optional<UserEntity> userOpt = userRepository.findAll()
                .stream()
                .filter(u -> u.getUsername().equals(request.getUsername()))
                .findFirst();

        if (userOpt.isEmpty()) return false;

        UserEntity user = userOpt.get();

        // Passwort prüfen
        return BCrypt.checkpw(request.getPassword(), user.getPasswordHash());
    }


}
