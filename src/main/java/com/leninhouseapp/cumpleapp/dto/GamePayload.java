package com.leninhouseapp.cumpleapp.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GamePayload {
    private Long gameId;
    private String gameName;
    private String gameType;
    private String gameStatus;
    private String description;
    private String rules;
    private GameState state;
    private QuestionDTO question; // solo se llena si gameType == TRIVIA y hay pregunta activa (sin la respuesta correcta)
}