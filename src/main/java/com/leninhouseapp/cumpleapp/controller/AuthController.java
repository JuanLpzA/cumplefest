package com.leninhouseapp.cumpleapp.controller;

import com.leninhouseapp.cumpleapp.entity.User;
import com.leninhouseapp.cumpleapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/home")
    public String home(Authentication auth) {
        if (auth == null) return "redirect:/login";
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TELEVISION"))) {
            return "redirect:/tv";
        }
        return "redirect:/player";
    }
}