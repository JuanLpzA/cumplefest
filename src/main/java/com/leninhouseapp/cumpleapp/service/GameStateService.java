package com.leninhouseapp.cumpleapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leninhouseapp.cumpleapp.dto.*;
import com.leninhouseapp.cumpleapp.entity.*;
import com.leninhouseapp.cumpleapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameStateService {

    private final GameRepository gameRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;
    private final ImpostorWordRepository impostorRepo;
    private final PointLogRepository pointLogRepo;
    private final SimpMessagingTemplate messaging;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GameState currentGameState = new GameState();

    public Optional<Game> getActiveGame() {
        return gameRepo.findByStatus(Game.GameStatus.ACTIVE);
    }

    public List<Game> getAllGames() {
        return gameRepo.findAllByOrderByOrderIndexAsc();
    }

    public List<User> getLeaderboard() {
        return userRepo.findByActiveTrueOrderByPointsDesc().stream()
                .filter(u -> u.getRole() != User.Role.TELEVISION)
                .limit(10)
                .collect(Collectors.toList());
    }

    @Transactional
    public void startGame(Long gameId) {
        gameRepo.findByStatus(Game.GameStatus.ACTIVE).ifPresent(g -> {
            g.setStatus(Game.GameStatus.FINISHED);
            gameRepo.save(g);
        });

        Game game = gameRepo.findById(gameId).orElseThrow();
        game.setStatus(Game.GameStatus.ACTIVE);
        currentGameState = new GameState();

        if (game.getType() == Game.GameType.JENGA_TOURNAMENT) {
            initJengaTournament();
        } else if (game.getType() == Game.GameType.TRIVIA) {
            initTrivia();
        } else if (game.getType() == Game.GameType.IMPOSTOR) {
            initImpostor();
        }

        gameRepo.save(game);
        broadcastGameUpdate();
    }

    @Transactional
    public void skipGame(Long gameId) {
        Game game = gameRepo.findById(gameId).orElseThrow();
        game.setStatus(Game.GameStatus.SKIPPED);
        gameRepo.save(game);
        currentGameState = new GameState();
        broadcastGameUpdate();
    }

    @Transactional
    public void finishGame(Long gameId) {
        Game game = gameRepo.findById(gameId).orElseThrow();
        game.setStatus(Game.GameStatus.FINISHED);
        gameRepo.save(game);
        currentGameState = new GameState();
        broadcastGameUpdate();
    }

    // ====================================================================
    // ---- JENGA TOURNAMENT (torneo de llaves real, ronda por ronda) ----
    // ====================================================================

    private void initJengaTournament() {
        List<User> players = userRepo.findByActiveTrueAndRoleNot(User.Role.TELEVISION);
        Collections.shuffle(players);

        List<JengaMatch> brackets = new ArrayList<>();
        long matchId = 1;
        for (int i = 0; i < players.size() - 1; i += 2) {
            brackets.add(JengaMatch.builder()
                    .matchId(matchId++)
                    .player1(players.get(i).getDisplayName())
                    .player1Id(players.get(i).getId())
                    .player2(players.get(i + 1).getDisplayName())
                    .player2Id(players.get(i + 1).getId())
                    .round(1)
                    .bye(false)
                    .build());
        }

        // Jugador impar: pasa directo (BYE), ya cuenta como "ganador" de ronda 1
        if (players.size() % 2 != 0) {
            User bye = players.get(players.size() - 1);
            brackets.add(JengaMatch.builder()
                    .matchId(matchId)
                    .player1(bye.getDisplayName())
                    .player1Id(bye.getId())
                    .player2("BYE")
                    .player2Id(null)
                    .winner(bye.getDisplayName())
                    .winnerId(bye.getId())
                    .round(1)
                    .bye(true)
                    .build());
        }

        currentGameState.setBrackets(brackets);
        currentGameState.setRound(1);
        currentGameState.setTournamentFinished(false);
        currentGameState.setChampionName(null);
    }

    /** Devuelve los partidos de la ronda actual que aún no tienen ganador (para que el admin elija). */
    public List<JengaMatch> getPendingMatchesCurrentRound() {
        int round = currentGameState.getRound();
        return currentGameState.getBrackets().stream()
                .filter(m -> m.getRound() == round && m.getWinnerId() == null)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setJengaWinner(Long matchId, Long winnerId) {
        User winner = userRepo.findById(winnerId).orElseThrow();
        List<JengaMatch> brackets = currentGameState.getBrackets();

        for (JengaMatch match : brackets) {
            if (Objects.equals(match.getMatchId(), matchId)) {
                match.setWinner(winner.getDisplayName());
                match.setWinnerId(winnerId);
                break;
            }
        }

        int currentRound = currentGameState.getRound();
        List<JengaMatch> roundMatches = brackets.stream()
                .filter(m -> m.getRound() == currentRound)
                .collect(Collectors.toList());

        boolean allDone = roundMatches.stream().allMatch(m -> m.getWinnerId() != null);

        if (allDone) {
            List<Long> winners = roundMatches.stream()
                    .map(JengaMatch::getWinnerId)
                    .collect(Collectors.toList());

            if (winners.size() == 1) {
                // Torneo terminado
                User champion = userRepo.findById(winners.get(0)).orElseThrow();
                currentGameState.setTournamentFinished(true);
                currentGameState.setChampionName(champion.getDisplayName());
                awardJengaPoints(roundMatches.get(0));
                Game game = getActiveGame().orElseThrow();
                game.setStatus(Game.GameStatus.FINISHED);
                gameRepo.save(game);
            } else {
                // Crear siguiente ronda emparejando a los ganadores
                int nextRound = currentRound + 1;
                Collections.shuffle(winners);
                long nextMatchId = brackets.stream().mapToLong(JengaMatch::getMatchId).max().orElse(0) + 1;

                for (int i = 0; i < winners.size() - 1; i += 2) {
                    User p1 = userRepo.findById(winners.get(i)).orElseThrow();
                    User p2 = userRepo.findById(winners.get(i + 1)).orElseThrow();
                    brackets.add(JengaMatch.builder()
                            .matchId(nextMatchId++)
                            .player1(p1.getDisplayName())
                            .player1Id(p1.getId())
                            .player2(p2.getDisplayName())
                            .player2Id(p2.getId())
                            .round(nextRound)
                            .bye(false)
                            .build());
                }
                // Si queda un número impar de ganadores, el último pasa directo (BYE)
                if (winners.size() % 2 != 0) {
                    User bye = userRepo.findById(winners.get(winners.size() - 1)).orElseThrow();
                    brackets.add(JengaMatch.builder()
                            .matchId(nextMatchId)
                            .player1(bye.getDisplayName())
                            .player1Id(bye.getId())
                            .player2("BYE")
                            .player2Id(null)
                            .winner(bye.getDisplayName())
                            .winnerId(bye.getId())
                            .round(nextRound)
                            .bye(true)
                            .build());
                }
                currentGameState.setRound(nextRound);
            }
        }

        broadcastGameUpdate();
    }

    private void awardJengaPoints(JengaMatch finalMatch) {
        Game game = getActiveGame().orElse(null);
        Long firstId = finalMatch.getWinnerId();
        Long secondId = Objects.equals(firstId, finalMatch.getPlayer1Id())
                ? finalMatch.getPlayer2Id()
                : finalMatch.getPlayer1Id();

        awardPoints(firstId, game != null ? game.getPointsFirst() : 15, game, "1er lugar Jenga");
        if (secondId != null) {
            awardPoints(secondId, game != null ? game.getPointsSecond() : 10, game, "2do lugar Jenga");
        }
    }

    // ====================================================================
    // ---- TRIVIA (cola sin repetición + flujo revelar respuesta) ----
    // ====================================================================

    private void initTrivia() {
        List<Question> questions = questionRepo.findByActiveTrueAndUsedFalse();
        Collections.shuffle(questions);
        currentGameState.setQuestionQueue(questions.stream().map(Question::getId).collect(Collectors.toList()));
        currentGameState.setPhase("IDLE");
        currentGameState.setPlayer1Id(null);
        currentGameState.setPlayer2Id(null);
        currentGameState.setBuzzedById(null);
        currentGameState.setCurrentQuestionId(null);
        currentGameState.setAnswerRevealed(false);
        currentGameState.setTriviaPairPool(new ArrayList<>());
    }

    /** Elige el siguiente par de jugadores sin repetir hasta que todos hayan jugado una vuelta completa. */
    private void pickNextPair() {
        List<User> allPlayers = userRepo.findByActiveTrueAndRoleNot(User.Role.TELEVISION);
        final List<Long> poolSoFar = currentGameState.getTriviaPairPool();

        // Si el pool está vacío o ya no quedan suficientes para formar par, se reinicia (nueva "vuelta")
        List<Long> available = allPlayers.stream()
                .map(User::getId)
                .filter(id -> !poolSoFar.contains(id))
                .collect(Collectors.toList());

        List<Long> pool;
        if (available.size() < 2) {
            pool = new ArrayList<>();
            available = allPlayers.stream().map(User::getId).collect(Collectors.toList());
        } else {
            pool = new ArrayList<>(poolSoFar);
        }

        Collections.shuffle(available);
        Long p1Id = available.get(0);
        Long p2Id = available.get(1);

        pool.add(p1Id);
        pool.add(p2Id);
        currentGameState.setTriviaPairPool(pool);

        User p1 = userRepo.findById(p1Id).orElseThrow();
        User p2 = userRepo.findById(p2Id).orElseThrow();
        currentGameState.setPlayer1Id(p1.getId());
        currentGameState.setPlayer1Name(p1.getDisplayName());
        currentGameState.setPlayer2Id(p2.getId());
        currentGameState.setPlayer2Name(p2.getDisplayName());
    }

    @Transactional
    public Map<String, Object> nextTriviaQuestion() {
        List<Long> queue = currentGameState.getQuestionQueue();
        if (queue == null || queue.isEmpty()) {
            // Se acabaron las preguntas: recargar el pool completo (las marcadas used=true se resetean)
            questionRepo.findByActiveTrue().forEach(q -> q.setUsed(false));
            List<Question> refreshed = questionRepo.findByActiveTrue();
            Collections.shuffle(refreshed);
            queue = refreshed.stream().map(Question::getId).collect(Collectors.toList());
            currentGameState.setQuestionQueue(queue);
            if (queue.isEmpty()) {
                return Map.of("error", "No hay preguntas configuradas");
            }
        }

        Long nextId = queue.remove(0);
        currentGameState.setQuestionQueue(queue);

        Question q = questionRepo.findById(nextId).orElseThrow();
        q.setUsed(true);
        questionRepo.save(q);

        pickNextPair();

        currentGameState.setCurrentQuestionId(q.getId());
        currentGameState.setCurrentQuestionText(q.getText());
        currentGameState.setCorrectAnswer(q.getCorrectAnswer());
        currentGameState.setAnswerRevealed(false);
        currentGameState.setBuzzedById(null);
        currentGameState.setBuzzedByName(null);
        currentGameState.setPhase("QUESTION");

        broadcastGameUpdate();
        return Map.of(
                "questionId", q.getId(),
                "player1", currentGameState.getPlayer1Name(),
                "player2", currentGameState.getPlayer2Name()
        );
    }

    @Transactional
    public boolean buzzer(Long userId) {
        String phase = currentGameState.getPhase();
        if (!"QUESTION".equals(phase)) return false;
        if (currentGameState.getBuzzedById() != null) return false;

        Long p1 = currentGameState.getPlayer1Id();
        Long p2 = currentGameState.getPlayer2Id();
        if (!userId.equals(p1) && !userId.equals(p2)) return false;

        User user = userRepo.findById(userId).orElseThrow();
        currentGameState.setBuzzedById(userId);
        currentGameState.setBuzzedByName(user.getDisplayName());
        currentGameState.setPhase("BUZZED");
        broadcastGameUpdate();
        return true;
    }

    /** El admin pulsa "Mostrar respuesta": revela la respuesta correcta en TV/celulares. */
    @Transactional
    public void revealTriviaAnswer() {
        currentGameState.setAnswerRevealed(true);
        currentGameState.setPhase("ANSWER_REVEALED");
        broadcastGameUpdate();
    }

    /**
     * El admin asigna quién ganó la ronda de esa pregunta (o nadie).
     * winnerId puede ser null si ninguno de los dos acertó.
     */
    @Transactional
    public void awardTriviaWinner(Long winnerId) {
        Game game = getActiveGame().orElse(null);
        if (winnerId != null) {
            awardPoints(winnerId, game != null ? game.getPointsFirst() : 1, game, "Trivia correcta");
        }
        currentGameState.setPhase("RESULT");
        broadcastGameUpdate();
    }

    public Question getCurrentTriviaQuestion() {
        Long qId = currentGameState.getCurrentQuestionId();
        if (qId == null) return null;
        return questionRepo.findById(qId).orElse(null);
    }

    // ====================================================================
    // ---- IMPOSTOR ----
    // ====================================================================

    @Transactional
    public Map<String, Object> initImpostor() {
        List<ImpostorWord> words = impostorRepo.findByUsedFalse();
        if (words.isEmpty()) {
            impostorRepo.findAll().forEach(w -> w.setUsed(false));
            impostorRepo.flush();
            words = impostorRepo.findByUsedFalse();
        }
        ImpostorWord word = words.get(new Random().nextInt(words.size()));
        word.setUsed(true);
        impostorRepo.save(word);

        int impostorCount = currentGameState.getImpostorCount() != null
                ? currentGameState.getImpostorCount() : 1;

        List<User> players = userRepo.findByActiveTrueAndRoleNot(User.Role.TELEVISION);
        Collections.shuffle(players);
        List<Long> impostorIds = players.subList(0, Math.min(impostorCount, players.size()))
                .stream().map(User::getId).collect(Collectors.toList());

        currentGameState.setWordId(word.getId());
        currentGameState.setWord(word.getWord());
        currentGameState.setHint(word.getHint());
        currentGameState.setImpostorIds(impostorIds);
        currentGameState.setPhase("PLAYING");

        broadcastGameUpdate();
        return Map.of("word", word.getWord(), "hint", word.getHint(), "impostorIds", impostorIds);
    }

    @Transactional
    public void awardImpostorPoints(List<Long> winnerIds) {
        Game game = getActiveGame().orElse(null);
        for (Long id : winnerIds) {
            awardPoints(id, game != null ? game.getPointsFirst() : 5, game, "Impostor ganador");
        }
        currentGameState.setPhase("RESULT");
        broadcastGameUpdate();
    }

    // ====================================================================
    // ---- PUNTOS ----
    // ====================================================================

    @Transactional
    public void awardPoints(Long userId, int points, Game game, String reason) {
        User user = userRepo.findById(userId).orElseThrow();
        user.setPoints(user.getPoints() + points);
        userRepo.save(user);

        PointLog log = PointLog.builder()
                .user(user).pointsDelta(points).reason(reason)
                .game(game).createdAt(LocalDateTime.now()).build();
        pointLogRepo.save(log);

        broadcastLeaderboard();
    }

    @Transactional
    public void adjustPoints(Long userId, int delta, String reason) {
        awardPoints(userId, delta, null, reason);
    }

    @Transactional
    public void awardGameResult(Long gameId, List<Long> orderedWinners) {
        Game game = gameRepo.findById(gameId).orElseThrow();
        int[] pts = {game.getPointsFirst(), game.getPointsSecond(), game.getPointsThird()};
        boolean[] hasGift = {game.isHasMinigiftFirst(), game.isHasMinigiftSecond(), game.isHasMinigiftThird()};

        for (int i = 0; i < orderedWinners.size() && i < 3; i++) {
            int p = pts[i];
            if (i == 0 && hasGift[0]) p = 0;
            if (p > 0) awardPoints(orderedWinners.get(i), p, game, (i + 1) + "° lugar en " + game.getName());
        }

        game.setStatus(Game.GameStatus.FINISHED);
        gameRepo.save(game);
        broadcastGameUpdate();
    }

    // ====================================================================
    // ---- RESET GENERAL (nuevo) ----
    // ====================================================================

    /** Reinicia puntajes de todos los jugadores y vuelve todos los juegos a PENDING. No borra usuarios ni preguntas. */
    @Transactional
    public void resetEverything() {
        // Finaliza cualquier juego activo
        currentGameState = new GameState();

        List<User> all = userRepo.findAll();
        for (User u : all) {
            u.setPoints(0);
        }
        userRepo.saveAll(all);

        List<Game> games = gameRepo.findAll();
        for (Game g : games) {
            g.setStatus(Game.GameStatus.PENDING);
        }
        gameRepo.saveAll(games);

        questionRepo.findAll().forEach(q -> q.setUsed(false));
        impostorRepo.findAll().forEach(w -> w.setUsed(false));

        pointLogRepo.deleteAll();

        broadcastGameUpdate();
        broadcastLeaderboard();
    }

    // ====================================================================
    // ---- BROADCAST ----
    // ====================================================================

    public void broadcastGameUpdate() {
        GamePayload payload = buildGamePayload();
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            messaging.convertAndSend("/topic/game", jsonPayload);
        } catch (Exception e) {
            log.error("Error broadcasting game update", e);
        }
    }

    public void broadcastLeaderboard() {
        List<User> lb = getLeaderboard();
        List<Map<String, Object>> leaderboardData = lb.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getDisplayName());
            map.put("points", u.getPoints());
            return map;
        }).collect(Collectors.toList());

        try {
            String jsonPayload = objectMapper.writeValueAsString(leaderboardData);
            messaging.convertAndSend("/topic/leaderboard", jsonPayload);
        } catch (Exception e) {
            log.error("Error broadcasting leaderboard", e);
        }
    }

    public GamePayload buildGamePayload() {
        Optional<Game> active = getActiveGame();

        if (active.isEmpty()) {
            return GamePayload.builder()
                    .gameId(null)
                    .gameName("Sin juego activo")
                    .gameType("NONE")
                    .gameStatus("IDLE")
                    .build();
        }

        Game g = active.get();
        GamePayload.GamePayloadBuilder builder = GamePayload.builder()
                .gameId(g.getId())
                .gameName(g.getName())
                .gameType(g.getType().name())
                .gameStatus(g.getStatus().name())
                .description(g.getDescription())
                .rules(g.getRules())
                .state(currentGameState);

        if (g.getType() == Game.GameType.TRIVIA) {
            Question q = getCurrentTriviaQuestion();
            if (q != null) {
                builder.question(QuestionDTO.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        // Solo se manda la respuesta correcta si ya fue revelada por el admin
                        .correctAnswer(currentGameState.isAnswerRevealed() ? q.getCorrectAnswer() : null)
                        .build());
            }
        }

        return builder.build();
    }

    public GameState getCurrentState() {
        return currentGameState;
    }

    public void setImpostorCount(int count) {
        currentGameState.setImpostorCount(count);
    }
}