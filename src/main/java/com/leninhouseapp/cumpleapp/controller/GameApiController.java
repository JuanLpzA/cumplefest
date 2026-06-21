package com.leninhouseapp.cumpleapp.controller;

import com.leninhouseapp.cumpleapp.entity.User;
import com.leninhouseapp.cumpleapp.repository.UserRepository;
import com.leninhouseapp.cumpleapp.service.GameStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameApiController {

    private final GameStateService gameStateService;
    private final UserRepository userRepo;

    @PostMapping("/buzzer")
    public ResponseEntity<?> buzzer(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        boolean ok = gameStateService.buzzer(user.getId());
        return ResponseEntity.ok(Map.of("success", ok));
    }

    @GetMapping("/state")
    public ResponseEntity<?> getState() {
        return ResponseEntity.ok(gameStateService.buildGamePayload());
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return ResponseEntity.ok(gameStateService.getLeaderboard().stream().map(u -> Map.of(
                "id", u.getId(), "name", u.getDisplayName(), "points", u.getPoints()
        )).toList());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getDisplayName(),
                "points", user.getPoints(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast() {
        gameStateService.broadcastGameUpdate();
        gameStateService.broadcastLeaderboard();
        return ResponseEntity.ok(Map.of("ok", true));
    }
}