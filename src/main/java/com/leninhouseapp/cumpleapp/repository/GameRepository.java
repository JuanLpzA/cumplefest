package com.leninhouseapp.cumpleapp.repository;

import com.leninhouseapp.cumpleapp.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<com.leninhouseapp.cumpleapp.entity.Game, Long> {
    Optional<Game> findByStatus(Game.GameStatus status);
    List<Game> findAllByOrderByOrderIndexAsc();
    List<Game> findByStatusOrderByOrderIndexAsc(Game.GameStatus status);
}
