package com.example.boulder_backend.service;

import com.example.boulder_backend.model.PasswordResetToken;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.PasswordResetTokenRepository;
import com.example.boulder_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepo;
    private final SecureRandom rng = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepo) {
        this.userRepository = userRepository;
        this.tokenRepo = tokenRepo;
    }

    /** Gibt das Roh-Token (nur für die E-Mail) oder null (wenn E-Mail unbekannt). */
    public String requestReset(String emailRaw, String ip, String ua) {
        String email = emailRaw.trim().toLowerCase(Locale.ROOT);
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return null;

        String rawToken = generateToken(32);
        String tokenHash = sha256Hex(rawToken);

        long now = System.currentTimeMillis();

        PasswordResetToken t = new PasswordResetToken();
        t.setId(UUID.randomUUID());                    // du nutzt UUID manuell
        t.setUserId(userOpt.get().getId());
        t.setTokenHash(tokenHash);
        t.setCreated(now);
        t.setExpires(now + 30L * 60_000L);             // 30 Minuten
        t.setRequestedIp(ip);
        t.setRequestedUserAgent(ua);

        tokenRepo.save(t);
        return rawToken;
    }

    public boolean validateToken(String rawToken) {
        return tokenRepo.findByTokenHash(sha256Hex(rawToken))
                .filter(t -> t.getUsedAt() == null && System.currentTimeMillis() < t.getExpires())
                .isPresent();
    }

    /** Verbraucht Token & setzt Passwort über Callback. */
    public void consumeTokenAndReset(String rawToken, String newPasswordHash, PasswordUpdater updater) {
        PasswordResetToken t = tokenRepo.findByTokenHash(sha256Hex(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("invalid"));
        long now = System.currentTimeMillis();
        if (t.getUsedAt() != null) throw new IllegalArgumentException("used");
        if (now >= t.getExpires()) throw new IllegalArgumentException("expired");

        updater.update(t.getUserId(), newPasswordHash);

        t.setUsedAt(now);
        tokenRepo.save(t);
    }

    public long cleanupExpired() {
        return tokenRepo.deleteByExpiresLessThan(System.currentTimeMillis());
    }

    // ---- helpers ----
    private String generateToken(int nBytes) {
        byte[] b = new byte[nBytes];
        rng.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b); // URL-safe
    }

    private String sha256Hex(String input) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte x : d) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface PasswordUpdater {
        void update(UUID userId, String newHash);
    }
}
