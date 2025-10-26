package com.example.boulder_backend.controller;


import com.example.boulder_backend.dto.*;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.UserRepository;
import com.example.boulder_backend.service.AuthService;
import com.example.boulder_backend.service.Mailer;
import com.example.boulder_backend.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth") // Basis-URL
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;



    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto request) {

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        String display = request.getUsername().trim();

        String norm    = display.toLowerCase(Locale.ROOT);

        request.setEmail(normalizedEmail);

        if (userRepository.existsByUsernameNorm(norm)) return ResponseEntity.status(409).body("username_taken");


        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("email_taken");
        }
        // Neuen User registrieren und als JSON zurückgeben
        UserEntity user = authService.register(request);
        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto request, @RequestHeader(value="X-Device-Id", required=false) String deviceId) {
        // AuthService prüft, ob Login gültig ist

        var tokens = authService.loginAndIssueTokens(request.getUsername().trim(), request.getPassword(), deviceId);

        if (tokens != null) return ResponseEntity.ok(tokens);
        return ResponseEntity.status(401).body("invalid_credentials");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody com.example.boulder_backend.dto.RefreshRequest req,
                                     @RequestHeader(value="X-Device-Id", required=false) String deviceId) {
        if (req == null || req.getRefreshToken() == null) return ResponseEntity.badRequest().body("missing_refresh_token");
        var tokens = authService.refreshTokens(req.getRefreshToken(), deviceId);
        if (tokens == null) return ResponseEntity.status(401).body("invalid_refresh");
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody com.example.boulder_backend.dto.LogoutRequest req) {
        if (req == null || req.getRefreshToken() == null) return ResponseEntity.badRequest().body("missing_refresh_token");
        authService.revokeRefresh(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/profile")
    public ResponseEntity<UserEntity> getProfile(@AuthenticationPrincipal Jwt principal) {
        if (principal == null) return ResponseEntity.status(401).build();
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
        if (principal == null) return ResponseEntity.status(401).build();

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

            String display = dto.getUsername().trim();
            String norm = display.toLowerCase(java.util.Locale.ROOT);


            // nur prüfen, wenn sich der Norm-Name ändert
            if (!norm.equals(user.getUsernameNorm())
                    && userRepository.existsByUsernameNormAndIdNot(norm, user.getId())) {
                return ResponseEntity.status(409).body("username_taken");
            }

            user.setUsername(display);
            user.setUsernameNorm(norm);
        }


        // Email aktualisieren, falls in Dto enthalten und nicht leer
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            String email = dto.getEmail().trim().toLowerCase(java.util.Locale.ROOT);

            Optional<UserEntity> existingUser = userRepository.findByEmail(email);

            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                return ResponseEntity.status(409).body("email_taken");
            }

            user.setEmail(email);
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

    private final PasswordResetService resetService;
    private final Mailer mailer;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest body,
            HttpServletRequest req,
            @RequestHeader(value = "User-Agent", required = false) String ua
    ) {
        String xff = req.getHeader("X-Forwarded-For");
        String clientIp = (xff != null && !xff.isBlank())
                ? xff.split(",")[0].trim()
                : req.getRemoteAddr();

        String rawToken = resetService.requestReset(body.getEmail(), clientIp, (ua != null ? ua : ""));

        if (rawToken != null) {
            String resetUrl = "https://sprayconnect.at/reset?token=" + rawToken; // ggf. Frontend-URL anpassen
            mailer.sendPasswordReset(body.getEmail().trim(), resetUrl);
        }
        // Immer OK zurück (kein E-Mail-Enumeration-Leak)
        return ResponseEntity.ok(java.util.Map.of("status", "ok"));
    }


    @GetMapping("/password/reset/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean valid = resetService.validateToken(token);
        return ResponseEntity.ok(java.util.Map.of("valid", valid));
    }

    @Transactional
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest body) {
        String newHash = passwordEncoder.encode(body.getNewPassword());
        resetService.consumeTokenAndReset(body.getToken(), newHash, (userId, hash) -> {
            var user = userRepository.findById(userId).orElseThrow();
            user.setPasswordHash(hash);
            userRepository.save(user);
        });
        return ResponseEntity.ok(java.util.Map.of("status", "password-updated"));
    }







}
