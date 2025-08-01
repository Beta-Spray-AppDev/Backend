package com.example.boulder_backend.controller;


import com.example.boulder_backend.dto.LoginDto;
import com.example.boulder_backend.dto.RegisterDto;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // Basis-URL
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody RegisterDto request) {
        // Neuen User registrieren und als JSON zurückgeben
        UserEntity user = authService.register(request);
        return ResponseEntity.ok(user);
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
}
