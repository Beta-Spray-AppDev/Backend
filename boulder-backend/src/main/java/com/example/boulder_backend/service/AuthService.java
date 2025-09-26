package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.LoginDto;
import com.example.boulder_backend.dto.RegisterDto;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.UserRepository;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;


import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;




@Service
@RequiredArgsConstructor //erstellt automatsich Konstruktor mit final-Feldern
public class AuthService {

    private final UserRepository userRepository; //Zugriff auf UserDb

    @Value("${jwt.secret}") // liest Wert aus application properties
    private String jwtSecret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    } // baut aus Secret einen HMAC-Key für HS256

    public UserEntity register(RegisterDto request) {

        //neues UserEntity-Objekt erstellen

        String display = request.getUsername().trim();
        String norm    = display.toLowerCase(java.util.Locale.ROOT);

        String email   = request.getEmail() == null ? null
                : request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);

        String password = request.getPassword();

        UserEntity user = new UserEntity();
        user.setUsername(display);
        user.setUsernameNorm(norm);
        user.setEmail(email);

        //pw hashen
        user.setPasswordHash(
                BCrypt.hashpw(password, BCrypt.gensalt())
        );
        user.setCreatedAt(System.currentTimeMillis());


        try {
            //user in db schreiben
            return userRepository.save(user);
        }

        // Falls zwischen Check in Controller und Save jemand denselben username/email registriert
        catch (DataIntegrityViolationException ex){
            throw ex;
        }

    }

    public String login(String username, String password) {

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        // User mit passendem Namen suchen
        String norm = username.trim().toLowerCase(java.util.Locale.ROOT);

        Optional<UserEntity> userOpt = userRepository.findByUsernameNorm(norm);
        if (userOpt.isEmpty()) return null;

        UserEntity user = userOpt.get();

        // Passwort prüfen
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            return generateToken(user); // Token erzeugen und zurückgeben
        }
        return null;

    }

    private String generateToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Gültig für 24h
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.get("userId", String.class));
    }





}
