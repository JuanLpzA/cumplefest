package com.leninhouseapp.cumpleapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "impostor_words")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImpostorWord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;
    private String hint; // pista para el impostor
    private String category;
    private boolean used = false;
}
