package com.leninhouseapp.cumpleapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "games")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 2000)
    private String rules;

    @Enumerated(EnumType.STRING)
    private GameType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GameStatus status = GameStatus.PENDING;

    private int pointsFirst;
    private int pointsSecond;
    private int pointsThird;

    private boolean hasMinigiftFirst;
    private boolean hasMinigiftSecond;
    private boolean hasMinigiftThird;

    private int orderIndex;

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String gameData = "{}";

    public enum GameType {
        JENGA_TOURNAMENT,    // Torneo de Jenga con llaves
        GUESS_CHARACTER,     // Adivina el personaje
        TRIVIA,              // Preguntas generales
        IMPOSTOR,            // El impostor
        BASIC                // Juegos básicos (solo registrar ganadores)
    }

    public enum GameStatus {
        PENDING,    // No iniciado
        ACTIVE,     // En progreso
        FINISHED,   // Terminado normalmente
        SKIPPED     // Saltado
    }
}