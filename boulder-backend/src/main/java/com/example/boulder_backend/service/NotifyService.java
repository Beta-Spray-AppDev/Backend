package com.example.boulder_backend.service;

import com.example.boulder_backend.model.Feedback;
import com.example.boulder_backend.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class NotifyService {

    @Value("${app.notifications.webhook:}")
    private String webhook;

    private final RestClient http = RestClient.create();

    // Feedback Notification
    @Async
    public void onNewFeedback(Feedback fb) {
        if (webhook == null || webhook.isBlank()) return;

        String content = """
            📬 Neues Feedback
            👤 User: %s
            ⭐ Sterne: %d
            📝 Text: %s
            🕒 Zeitpunkt: %s
            🧩 Version: %s
            📱 Device: %s
            """.formatted(
                safe(fb.getUsername(), "(unbekannt)"),
                fb.getStars(),
                safe(fb.getMessage(), "(kein Text)"),
                FORMATTER.format(Instant.ofEpochMilli(fb.getCreatedAt())),
                safe(fb.getAppVersion(), "-"),
                safe(fb.getDeviceInfo(), "-")
        );


        http.post()
                .uri(webhook)
                .body(new DiscordPayload(content))
                .retrieve()
                .toBodilessEntity();
    }

    // 👤 Neue Registrierung Notification
    @Async
    public void onNewUser(UserEntity user) {
        if (webhook == null || webhook.isBlank()) return;

        String content = """
                🆕 **Neue Registrierung**
                👤 Benutzer: %s
                📧 Email: %s
                🕒 Zeitpunkt: %s
                """.formatted(
                user.getUsername(),
                user.getEmail() == null ? "(keine E-Mail)" : user.getEmail(),
                FORMATTER.format(Instant.ofEpochMilli(user.getCreatedAt()))
        );

        http.post()
                .uri(webhook)
                .body(new DiscordPayload(content))
                .retrieve()
                .toBodilessEntity();
    }

    private String safe(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    // Wird für Discord JSON benötigt
    public record DiscordPayload(String content) {}


    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());
}
