package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.SpraywallDto;
import com.example.boulder_backend.service.AuthService;
import com.example.boulder_backend.service.SpraywallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/spraywalls")
public class SpraywallController {

    private static final Logger log = LoggerFactory.getLogger(SpraywallController.class);

    private final SpraywallService spraywallService;
    private final AuthService authService;

    public SpraywallController(SpraywallService spraywallService, AuthService authService) {
        this.spraywallService = spraywallService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<SpraywallDto> createSpraywall(
            @RequestBody SpraywallDto dto,
            @RequestHeader("Authorization") String authHeader) {

        UUID currentUserId = authService.extractUserId(authHeader);
        log.info("CREATE spraywall user={} gymId={} name='{}' publicVisible={} archived={}",
                currentUserId, dto.getGymId(), dto.getName(), dto.isPublicVisible(), dto.isArchived());

        var saved = spraywallService.createSpraywall(dto, currentUserId);
        var out = spraywallService.toDto(saved);

        log.info("CREATE result id={} gymId={} archived={}", out.getId(), out.getGymId(), out.isArchived());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<SpraywallDto>> getSpraywallsByGym(
            @PathVariable UUID gymId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "archived", defaultValue = "false") boolean archived) {

        UUID userId = authService.extractUserId(authHeader);
        log.info("LIST spraywalls gymId={} archived={} user={}", gymId, archived, userId);

        List<SpraywallDto> list = archived
                ? spraywallService.getArchivedVisibleByGym(gymId, userId)
                : spraywallService.getAllVisibleByGym(gymId, userId);

        var sample = list.stream()
                .limit(5)
                .map(s -> s.getId() + ":'" + s.getName() + "'")
                .toList();

        log.info("LIST result count={} sample={}", list.size(), sample);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpraywallDto> getSpraywallById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authService.extractUserId(authHeader);
        log.info("DETAIL spraywall id={} user={}", id, userId);

        SpraywallDto dto = spraywallService.getVisibleById(id, userId);
        if (dto == null) {
            log.info("DETAIL not found id={}", id);
            return ResponseEntity.notFound().build();
        }
        log.info("DETAIL result id={} name='{}' archived={}", dto.getId(), dto.getName(), dto.isArchived());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/gym/{gymId}/{spraywallId}/archive")
    public ResponseEntity<Void> setArchived(
            @PathVariable UUID gymId,
            @PathVariable UUID spraywallId,
            @RequestParam boolean archived,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authService.extractUserId(authHeader);
        log.info("ARCHIVE toggle gymId={} spraywallId={} -> archived={} by user={}",
                gymId, spraywallId, archived, userId);

        spraywallService.setArchived(spraywallId, archived, userId);
        log.info("ARCHIVE done spraywallId={} archived={}", spraywallId, archived);

        return ResponseEntity.noContent().build();
    }
}
