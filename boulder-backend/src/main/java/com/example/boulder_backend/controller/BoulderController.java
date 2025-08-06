package com.example.boulder_backend.controller;

import com.example.boulder_backend.dto.BoulderDto;
import com.example.boulder_backend.dto.HoldDto;
import com.example.boulder_backend.model.Boulder;
import com.example.boulder_backend.model.Hold;
import com.example.boulder_backend.model.Spraywall;
import com.example.boulder_backend.model.UserEntity;
import com.example.boulder_backend.repository.BoulderRepository;
import com.example.boulder_backend.repository.SpraywallRepository;
import com.example.boulder_backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/boulders")
@RequiredArgsConstructor
public class BoulderController {

    private final BoulderRepository boulderRepository;
    private final SpraywallRepository spraywallRepository;
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping
    public ResponseEntity<Void> createBoulder(@RequestBody BoulderDto dto, @RequestHeader("Authorization") String authHeader) {


        System.out.println("Boulder-Request empfangen: " + dto);
        String token = authHeader.replace("Bearer ", "");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        UUID userId = UUID.fromString(claims.get("userId", String.class));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Boulder boulder = new Boulder();
        boulder.setId(UUID.randomUUID());
        boulder.setName(dto.getName());
        boulder.setDifficulty(dto.getDifficulty());
        boulder.setCreatedAt(System.currentTimeMillis());
        boulder.setLastUpdated(System.currentTimeMillis());
        boulder.setCreatedBy(user);

        Spraywall spraywall = spraywallRepository.findById(dto.getSpraywallId())
                .orElseThrow(() -> new RuntimeException("Spraywall nicht gefunden"));
        boulder.setSpraywall(spraywall);

        List<Hold> holds = dto.getHolds().stream().map(holdDto -> {
            Hold hold = new Hold();
            hold.setId(holdDto.getId());
            hold.setX(holdDto.getX());
            hold.setY(holdDto.getY());
            hold.setType(holdDto.getType());
            hold.setBoulder(boulder);
            return hold;
        }).toList();

        boulder.setHolds(holds);
        boulderRepository.save(boulder);

        return ResponseEntity.ok().build();
    }





    @GetMapping("/spraywall/{spraywallId}")
    public ResponseEntity<List<BoulderDto>> getBouldersBySpraywall(@PathVariable UUID spraywallId) {
        List<Boulder> boulders = boulderRepository.findBySpraywallId(spraywallId);

        List<BoulderDto> result = boulders.stream().map(boulder -> {
            BoulderDto dto = new BoulderDto();
            dto.setName(boulder.getName());
            dto.setDifficulty(boulder.getDifficulty());
            dto.setSpraywallId(boulder.getSpraywall().getId());

            dto.setHolds(
                    boulder.getHolds().stream().map(hold -> {
                        HoldDto holdDto = new HoldDto();
                        holdDto.setId(hold.getId());
                        holdDto.setX(hold.getX());
                        holdDto.setY(hold.getY());
                        holdDto.setType(hold.getType());
                        return holdDto;
                    }).toList()
            );

            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }

}
