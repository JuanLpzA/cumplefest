package com.leninhouseapp.cumpleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PointLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private int pointsDelta; // positivo o negativo
    private String reason;
    private LocalDateTime createdAt;

    @ManyToOne
    private Game game; // puede ser null si es ajuste manual
}
