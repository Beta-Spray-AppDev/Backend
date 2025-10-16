package com.example.boulder_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class Mailer {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String from;

    public Mailer(JavaMailSender mailSender,
                  TemplateEngine templateEngine,
                  @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
    }

    public void sendPasswordReset(String toEmail, String resetUrl) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Passwort zurücksetzen");

            var ctx = new Context();
            ctx.setVariable("resetUrl", resetUrl);
            ctx.setVariable("year", java.time.Year.now().getValue());

            String html = templateEngine.process("password-reset", ctx);

            String plain = """
                Hallo,

                du hast das Zurücksetzen deines Passworts angefordert.
                Link (30 Minuten gültig): %s

                Falls der Button nicht funktioniert, kopiere den Link.
                (Bitte mit dem Handy aufmachen)
                Für jedes Mal Passwort zurücksetzen 20 Klimmzüge ;)
            """.formatted(resetUrl);

            helper.setText(plain, html);

            helper.addInline("logo", new ClassPathResource("static/logo.png"), "image/png");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Senden der E-Mail", e);
        }
    }
}