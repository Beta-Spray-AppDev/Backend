package com.example.boulder_backend.service;

import com.example.boulder_backend.model.Feedback;
import com.example.boulder_backend.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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

    // RestClient mit Default-Header
    private final RestClient http = RestClient.builder()
            .defaultHeader("User-Agent", "SprayConnect/1.0 (+raspberrypi)")
            .build();

    // ---- Helper: sicher loggen (Token maskieren) ----
    private String mask(String url) {
        if (url == null) return "(null)";
        return url.replaceAll("([a-zA-Z0-9_-]{8}).+$", "$1********");
    }

    // ---- Gemeinsamer Sender mit Timeouts/Retry/Logging ----
    private void send(String content) {
        if (webhook == null || webhook.isBlank()) {
            // wichtig: im Container sieht man das sofort
            System.err.println("Discord Webhook URL fehlt! Property: app.notifications.webhook");
            return;
        }
        try {
            http.post()
                    .uri(webhook) // absolute URL ok
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DiscordPayload(content))
                    .retrieve()
                    .toBodilessEntity(); // blockt synchron; @Async verschiebt in Threadpool
            System.out.println("Discord OK -> " + mask(webhook));
        } catch (org.springframework.web.client.RestClientResponseException e) {
            // HTTP-Status ungleich 2xx
            System.err.println("Discord HTTP " + e.getStatusCode() + " -> " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Discord Fehler: " + e);
            // optional kurzer Retry:
            try {
                Thread.sleep(300);
                http.post().uri(webhook)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new DiscordPayload(content))
                        .retrieve()
                        .toBodilessEntity();
                System.out.println("Discord OK nach Retry -> " + mask(webhook));
            } catch (Exception ignored) {
                System.err.println("Discord endgültig fehlgeschlagen.");
            }
        }
    }

    // Feedback Notification
    @Async
    public void onNewFeedback(Feedback fb) {
        var msg = """
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
        send(msg);
    }

    // Neue Registrierung
    @Async
    public void onNewUser(UserEntity user) {
        var msg = """
            🆕 **Neue Registrierung**
            👤 Benutzer: %s
            📧 Email: %s
            🕒 Zeitpunkt: %s
            """.formatted(
                user.getUsername(),
                user.getEmail() == null ? "(keine E-Mail)" : user.getEmail(),
                FORMATTER.format(Instant.ofEpochMilli(user.getCreatedAt()))
        );
        send(msg);
    }

    // JSON-Record für Discord
    public record DiscordPayload(String content) {}

    private String safe(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());
}

