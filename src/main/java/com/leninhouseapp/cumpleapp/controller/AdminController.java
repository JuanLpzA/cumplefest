package com.leninhouseapp.cumpleapp.controller;

import com.leninhouseapp.cumpleapp.entity.*;
import com.leninhouseapp.cumpleapp.repository.*;
import com.leninhouseapp.cumpleapp.service.GameStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GameRepository gameRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;
    private final ImpostorWordRepository impostorRepo;
    private final GameStateService gameStateService;
    private final PasswordEncoder encoder;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("games", gameStateService.getAllGames());
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("leaderboard", gameStateService.getLeaderboard());
        model.addAttribute("activeGame", gameStateService.getActiveGame().orElse(null));
        model.addAttribute("gameState", gameStateService.getCurrentState());
        model.addAttribute("pendingMatches", gameStateService.getPendingMatchesCurrentRound());
        return "admin/dashboard";
    }

    // ---- JUEGOS ----
    @PostMapping("/game/start/{id}")
    public String startGame(@PathVariable Long id, RedirectAttributes ra) {
        gameStateService.startGame(id);
        ra.addFlashAttribute("msg", "Juego iniciado");
        return "redirect:/admin";
    }

    @PostMapping("/game/skip/{id}")
    public String skipGame(@PathVariable Long id, RedirectAttributes ra) {
        gameStateService.skipGame(id);
        ra.addFlashAttribute("msg", "Juego saltado");
        return "redirect:/admin";
    }

    @PostMapping("/game/finish/{id}")
    public String finishGame(@PathVariable Long id, RedirectAttributes ra) {
        gameStateService.finishGame(id);
        ra.addFlashAttribute("msg", "Juego terminado");
        return "redirect:/admin";
    }

    @GetMapping("/game/edit/{id}")
    public String editGame(@PathVariable Long id, Model model) {
        model.addAttribute("game", gameRepo.findById(id).orElseThrow());
        return "admin/edit-game";
    }

    @PostMapping("/game/edit/{id}")
    public String saveGame(@PathVariable Long id, @ModelAttribute Game game, RedirectAttributes ra) {
        Game existing = gameRepo.findById(id).orElseThrow();
        existing.setName(game.getName());
        existing.setDescription(game.getDescription());
        existing.setRules(game.getRules());
        existing.setPointsFirst(game.getPointsFirst());
        existing.setPointsSecond(game.getPointsSecond());
        existing.setPointsThird(game.getPointsThird());
        existing.setHasMinigiftFirst(game.isHasMinigiftFirst());
        existing.setHasMinigiftSecond(game.isHasMinigiftSecond());
        existing.setHasMinigiftThird(game.isHasMinigiftThird());
        gameRepo.save(existing);
        ra.addFlashAttribute("msg", "Juego actualizado");
        return "redirect:/admin";
    }

    @PostMapping("/game/new")
    public String newGame(@RequestParam String name,
                          @RequestParam String description,
                          @RequestParam String rules,
                          @RequestParam int pointsFirst,
                          @RequestParam int pointsSecond,
                          @RequestParam int pointsThird,
                          @RequestParam(defaultValue = "false") boolean hasMinigiftFirst,
                          RedirectAttributes ra) {
        long maxOrder = gameRepo.findAll().stream().mapToInt(Game::getOrderIndex).max().orElse(0);
        Game game = Game.builder()
                .name(name).description(description).rules(rules)
                .type(Game.GameType.BASIC).status(Game.GameStatus.PENDING)
                .pointsFirst(pointsFirst).pointsSecond(pointsSecond).pointsThird(pointsThird)
                .hasMinigiftFirst(hasMinigiftFirst)
                .orderIndex((int) maxOrder + 1).gameData("{}").build();
        gameRepo.save(game);
        ra.addFlashAttribute("msg", "Juego creado");
        return "redirect:/admin";
    }

    // ---- RESULTADOS MANUALES (juegos BASIC) ----
    @PostMapping("/game/award")
    public String awardResult(@RequestParam Long gameId,
                              @RequestParam(required = false) Long first,
                              @RequestParam(required = false) Long second,
                              @RequestParam(required = false) Long third,
                              RedirectAttributes ra) {
        List<Long> winners = new java.util.ArrayList<>();
        if (first != null) winners.add(first);
        if (second != null) winners.add(second);
        if (third != null) winners.add(third);
        gameStateService.awardGameResult(gameId, winners);
        ra.addFlashAttribute("msg", "Puntos asignados");
        return "redirect:/admin";
    }

    // ---- PUNTOS MANUALES ----
    @PostMapping("/points/adjust")
    public String adjustPoints(@RequestParam Long userId, @RequestParam int delta,
                               @RequestParam String reason, RedirectAttributes ra) {
        gameStateService.adjustPoints(userId, delta, reason);
        ra.addFlashAttribute("msg", "Puntos ajustados");
        return "redirect:/admin";
    }

    // ---- TRIVIA ----
    @GetMapping("/trivia/manage")
    public String triviaManage(Model model) {
        model.addAttribute("questions", questionRepo.findAll());
        return "admin/trivia";
    }

    @PostMapping("/trivia/add")
    public String addQuestion(@RequestParam String text,
                              @RequestParam String optionA, @RequestParam String optionB,
                              @RequestParam String optionC, @RequestParam String optionD,
                              @RequestParam String correctAnswer, RedirectAttributes ra) {
        questionRepo.save(Question.builder().text(text).optionA(optionA).optionB(optionB)
                .optionC(optionC).optionD(optionD).correctAnswer(correctAnswer).build());
        ra.addFlashAttribute("msg", "Pregunta añadida");
        return "redirect:/admin/trivia/manage";
    }

    @PostMapping("/trivia/delete/{id}")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes ra) {
        questionRepo.deleteById(id);
        ra.addFlashAttribute("msg", "Pregunta eliminada");
        return "redirect:/admin/trivia/manage";
    }

    @PostMapping("/trivia/next")
    public String nextQuestion(RedirectAttributes ra) {
        gameStateService.nextTriviaQuestion();
        ra.addFlashAttribute("msg", "Siguiente pregunta y nueva pareja seleccionada");
        return "redirect:/admin";
    }

    @PostMapping("/trivia/reveal")
    public String revealAnswer(RedirectAttributes ra) {
        gameStateService.revealTriviaAnswer();
        ra.addFlashAttribute("msg", "Respuesta revelada");
        return "redirect:/admin";
    }

    @PostMapping("/trivia/award")
    public String awardTrivia(@RequestParam(required = false) Long winnerId, RedirectAttributes ra) {
        gameStateService.awardTriviaWinner(winnerId);
        ra.addFlashAttribute("msg", winnerId != null ? "Punto asignado" : "Ronda sin ganador registrada");
        return "redirect:/admin";
    }

    // ---- JENGA ----
    @PostMapping("/jenga/winner")
    public String jengaWinner(@RequestParam Long matchId, @RequestParam Long winnerId, RedirectAttributes ra) {
        gameStateService.setJengaWinner(matchId, winnerId);
        ra.addFlashAttribute("msg", "Ganador de llave registrado");
        return "redirect:/admin";
    }

    // ---- IMPOSTOR ----
    @PostMapping("/impostor/start")
    public String startImpostor(@RequestParam int impostorCount, RedirectAttributes ra) {
        gameStateService.setImpostorCount(impostorCount);
        gameStateService.initImpostor();
        ra.addFlashAttribute("msg", "Ronda de impostor iniciada");
        return "redirect:/admin";
    }

    @PostMapping("/impostor/award")
    public String awardImpostor(@RequestParam(required = false) List<Long> winnerIds, RedirectAttributes ra) {
        gameStateService.awardImpostorPoints(winnerIds != null ? winnerIds : List.of());
        ra.addFlashAttribute("msg", "Puntos de impostor asignados");
        return "redirect:/admin";
    }

    // ---- USUARIOS ----
    @PostMapping("/user/new")
    public String createUser(@RequestParam String username, @RequestParam String displayName, RedirectAttributes ra) {
        if (userRepo.findByUsername(username).isPresent()) {
            ra.addFlashAttribute("error", "Usuario ya existe");
            return "redirect:/admin";
        }
        userRepo.save(User.builder().username(username).displayName(displayName)
                .password(encoder.encode("123456")).role(User.Role.PLAYER).active(true).points(0).build());
        ra.addFlashAttribute("msg", "Usuario creado con contraseña 123456");
        return "redirect:/admin";
    }

    @PostMapping("/user/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        User u = userRepo.findById(id).orElseThrow();
        u.setActive(!u.isActive());
        userRepo.save(u);
        ra.addFlashAttribute("msg", "Usuario " + (u.isActive() ? "activado" : "desactivado"));
        return "redirect:/admin";
    }

    @PostMapping("/user/rename/{id}")
    public String renameUser(@PathVariable Long id, @RequestParam String displayName, RedirectAttributes ra) {
        User u = userRepo.findById(id).orElseThrow();
        u.setDisplayName(displayName);
        userRepo.save(u);
        ra.addFlashAttribute("msg", "Nombre actualizado");
        return "redirect:/admin";
    }

    // ---- RESET GENERAL ----
    @PostMapping("/reset")
    public String resetEverything(RedirectAttributes ra) {
        gameStateService.resetEverything();
        ra.addFlashAttribute("msg", "Todo reiniciado: puntajes en 0 y juegos en pendiente");
        return "redirect:/admin";
    }
}