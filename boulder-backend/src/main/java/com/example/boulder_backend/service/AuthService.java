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

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public UserEntity register(RegisterDto request) {

        //neues UserEntity-Objekt erstellen

        String username = request.getUsername().trim();

        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();

        String password = request.getPassword();

        UserEntity user = new UserEntity();
        user.setUsername(username);
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
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
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
