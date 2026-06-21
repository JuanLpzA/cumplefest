package com.leninhouseapp.cumpleapp.controller;

import com.leninhouseapp.cumpleapp.entity.*;
import com.leninhouseapp.cumpleapp.repository.*;
import com.leninhouseapp.cumpleapp.service.GameStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PlayerController {

    private final UserRepository userRepo;
    private final GameStateService gameStateService;

    @GetMapping("/player")
    public String player(Model model, Authentication auth) {
        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("leaderboard", gameStateService.getLeaderboard());
        model.addAttribute("games", gameStateService.getAllGames());

        Optional<Game> activeGame = gameStateService.getActiveGame();
        activeGame.ifPresent(g -> {
            model.addAttribute("activeGame", g);
            model.addAttribute("gameState", gameStateService.buildGamePayload());
        });

        return "player/index";
    }

    @GetMapping("/player/game-info")
    public String gameInfo(Model model) {
        Optional<Game> activeGame = gameStateService.getActiveGame();
        activeGame.ifPresent(g -> {
            model.addAttribute("activeGame", g);
        });
        return "player/game-info";
    }
}