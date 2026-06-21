package com.leninhouseapp.cumpleapp.dto;

import lombok.*;

import java.util.List;

/**
 * Estado en memoria del juego que está activo en un momento dado.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GameState {

    // ---- JENGA_TOURNAMENT ----
    @Builder.Default
    private List<JengaMatch> brackets = new java.util.ArrayList<>();
    private int round;
    /** true cuando ya hay un único ganador y el torneo terminó */
    private boolean tournamentFinished;
    private String championName;

    // ---- TRIVIA ----
    @Builder.Default
    private List<Long> questionQueue = new java.util.ArrayList<>(); // preguntas restantes, sin repetir
    private String phase; // IDLE, QUESTION, BUZZED, ANSWER_REVEALED, RESULT
    private Long currentQuestionId;
    private String currentQuestionText;
    private String correctAnswer; // solo se manda al admin, no a player/tv hasta revelar
    private boolean answerRevealed;

    private Long player1Id;
    private String player1Name;
    private Long player2Id;
    private String player2Name;
    private Long buzzedById;
    private String buzzedByName;

    /** ids de jugadores que ya compitieron en esta "vuelta" de emparejamientos, para no repetir hasta agotar */
    @Builder.Default
    private List<Long> triviaPairPool = new java.util.ArrayList<>();

    // ---- IMPOSTOR ----
    private Long wordId;
    private String word;
    private String hint;
    @Builder.Default
    private List<Long> impostorIds = new java.util.ArrayList<>();
    private Integer impostorCount;
}