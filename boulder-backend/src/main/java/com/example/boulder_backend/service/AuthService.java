package com.example.boulder_backend.service;

import com.example.boulder_backend.dto.LoginDto;
import com.example.boulder_backend.dto.RegisterDto;
import com.example.boulder_backend.dto.TokenResponse;
import com.example.boulder_backend.model.RefreshToken;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.RefreshTokenRepository;
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

    private static final long ACCESS_TTL_MS = 15 * 60 * 1000; // 15 Minuten
    private static final long REFRESH_TTL_MS = 30L * 24 * 60 * 60 * 1000; // 30 Tage

    private String hash(String value) {
        return BCrypt.hashpw(value, BCrypt.gensalt()); // bcrypt als simpler Hash
    }
    private boolean matches(String raw, String hashed) {
        return BCrypt.checkpw(raw, hashed);
    }

    private final RefreshTokenRepository refreshTokenRepository;


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

    public TokenResponse  loginAndIssueTokens(String username, String password, String deviceId) {

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        // User mit passendem Namen suchen
        String norm = username.trim().toLowerCase(java.util.Locale.ROOT);

        Optional<UserEntity> userOpt = userRepository.findByUsernameNorm(norm);
        if (userOpt.isEmpty()) return null;

        UserEntity user = userOpt.get();

        // Passwort prüfen
        if (!BCrypt.checkpw(password, user.getPasswordHash())) return null;
        return issueTokens(user, deviceId);

    }

    public TokenResponse refreshTokens(String refreshTokenRaw, String deviceId) {
        String h = sha256(refreshTokenRaw);
        var rtOpt = refreshTokenRepository.findByTokenHash(h);
        if (rtOpt.isEmpty()) return null;
        var rt = rtOpt.get();
        if (rt.isRevoked()) return null;
        if (rt.getExpiresAt().isBefore(java.time.Instant.now())) return null;
        // (optional) deviceId prüfen, wenn gesetzt:
        if (rt.getDeviceId() != null && deviceId != null && !rt.getDeviceId().equals(deviceId)) {
            return null;
        }
        // Rotation: altes revoke + neues erzeugen
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        var user = rt.getUser();
        return issueTokens(user, deviceId);
    }

    private String sha256(String raw) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void revokeRefresh(String refreshTokenRaw) {
        String h = sha256(refreshTokenRaw);
        refreshTokenRepository.findByTokenHash(h).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }




    private String generateAccessToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TTL_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String newOpaqueToken() {
        return UUID.randomUUID().toString() + "." + UUID.randomUUID(); // ausreichend random
    }

    private String issueRefreshToken(UserEntity user, String deviceId) {
        String raw = newOpaqueToken();
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setIssuedAt(java.time.Instant.now());
        rt.setExpiresAt(java.time.Instant.ofEpochMilli(System.currentTimeMillis() + REFRESH_TTL_MS));
        rt.setTokenHash(sha256(raw));
        rt.setDeviceId(deviceId);
        refreshTokenRepository.save(rt);
        return raw; // nur Klartext an Client
    }

    public TokenResponse issueTokens(UserEntity user, String deviceId) {
        String access = generateAccessToken(user);
        String refresh = issueRefreshToken(user, deviceId);
        return new TokenResponse(
                access, ACCESS_TTL_MS/1000,
                refresh, REFRESH_TTL_MS/1000
        );
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
