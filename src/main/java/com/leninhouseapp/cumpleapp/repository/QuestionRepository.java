package com.leninhouseapp.cumpleapp.repository;

import com.leninhouseapp.cumpleapp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByActiveTrue();

    List<Question> findByActiveTrueAndUsedFalse();
}
