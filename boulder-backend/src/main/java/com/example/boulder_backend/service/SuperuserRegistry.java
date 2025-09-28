package com.example.boulder_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SuperuserRegistry {

    private static final Logger log = LoggerFactory.getLogger(SuperuserRegistry.class);

    private final ObjectMapper mapper;
    private Set<UUID> superusers = Set.of();

    public SuperuserRegistry(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    void init() {
        try (InputStream is = new ClassPathResource("superuser.json").getInputStream()) {
            List<String> ids = mapper.readValue(is, new TypeReference<List<String>>() {});
            superusers = ids.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toUnmodifiableSet());
            log.info("Loaded {} superusers from classpath.", superusers.size());
        } catch (Exception e) {
            log.warn("No superusers.json found or invalid JSON. Running with 0 superusers.", e);
            superusers = Set.of();
        }
    }

    public boolean isSuperuser(UUID userId) {
        boolean ok = userId != null && superusers.contains(userId);
        log.debug("isSuperuser({}) -> {}", userId, ok);
        return ok;
    }
}
