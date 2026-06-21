package com.leninhouseapp.cumpleapp.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestionDTO {
    private Long id;
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    /** null hasta que el admin pulsa "Mostrar respuesta" */
    private String correctAnswer;
}