package com.example.boulder_backend.controller;


import com.example.boulder_backend.dto.LoginDto;
import com.example.boulder_backend.dto.RegisterDto;
import com.example.boulder_backend.dto.UpdateProfileDto;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.UserRepository;
import com.example.boulder_backend.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth") // Basis-URL
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body("username_taken");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("email_taken");
        }
        // Neuen User registrieren und als JSON zurückgeben
        UserEntity user = authService.register(request);
        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto request) {
        // AuthService prüft, ob Login gültig ist
        String token = authService.login(request);

        if (token != null) {
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("Login fehlgeschlagen");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserEntity> getProfile(@AuthenticationPrincipal Jwt principal) {
        // Holt userid aus token bei .claim
        UUID userId = UUID.fromString(principal.getClaimAsString("userId"));
        // Sucht in db nach dem user
        Optional<UserEntity> userOpt = userRepository.findById(userId);

        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }



    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal Jwt principal,
            @RequestBody UpdateProfileDto dto //neue Daten vom Client
    ) {

        // Holt UserID aus Token
        UUID userId = UUID.fromString(principal.getClaimAsString("userId"));

        // Sucht in db nach User
        Optional<UserEntity> userOpt = userRepository.findById(userId);

        // Falls User nicht existiert
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = userOpt.get();

        // Username aktualisieren, falls in Dto enthalten und nicht leer
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            Optional<UserEntity> existingUser = userRepository.findByUsername(dto.getUsername());

            // Wenn jemand anderes diesen Namen hat Fehler zurückgeben
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {


                return ResponseEntity.status(409).body("username_taken");            }

            // Sonst: Username setzen
            user.setUsername(dto.getUsername());
        }

        // Email aktualisieren, falls in Dto enthalten und nicht leer
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            Optional<UserEntity> existingUser = userRepository.findByEmail(dto.getEmail());

            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                return ResponseEntity.status(409).body("email_taken");
            }

            user.setEmail(dto.getEmail());
        }


        // Passwort aktualisieren, falls in Dto enthalten und nicht leer
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {

            // PW hashen
            user.setPasswordHash(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        }

        // In DB aktualisieren
        UserEntity updated = userRepository.save(user);

        return ResponseEntity.ok(updated);
    }





}
