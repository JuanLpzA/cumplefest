package com.leninhouseapp.cumpleapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private int points = 0;

    public enum Role {
        ADMIN,        // Diego - puede configurar todo
        PLAYER,       // Jugadores normales
        TELEVISION    // Vista de TV
    }
}