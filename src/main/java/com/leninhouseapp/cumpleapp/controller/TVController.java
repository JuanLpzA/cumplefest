package com.leninhouseapp.cumpleapp.controller;

import com.leninhouseapp.cumpleapp.service.GameStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
@RequiredArgsConstructor
public class TVController {

    private final GameStateService gameStateService;

    @GetMapping("/tv")
    public String tv(Model model) {
        model.addAttribute("leaderboard", gameStateService.getLeaderboard());
        model.addAttribute("gamePayload", gameStateService.buildGamePayload());
        return "tv/index";
    }
}
