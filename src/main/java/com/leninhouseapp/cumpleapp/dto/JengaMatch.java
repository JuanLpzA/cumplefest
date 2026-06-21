package com.leninhouseapp.cumpleapp.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JengaMatch {
    private Long matchId;
    private String player1;
    private Long player1Id;
    private String player2;
    private Long player2Id;
    private String winner;
    private Long winnerId;
    private int round;
    /** true si es un BYE (pase directo, sin rival) */
    private boolean bye;
}