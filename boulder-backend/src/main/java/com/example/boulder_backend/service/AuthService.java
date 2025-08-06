package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.LoginDto;
import com.example.boulder_backend.dto.RegisterDto;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.UserRepository;


import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;


import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
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

    public String login(LoginDto request) {
        // User mit passendem Namen suchen
        Optional<UserEntity> userOpt = userRepository.findAll()
                .stream()
                .filter(u -> u.getUsername().equals(request.getUsername()))
                .findFirst();

        if (userOpt.isEmpty()) return null;

        UserEntity user = userOpt.get();

        // Passwort prüfen
        if (BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
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


}
