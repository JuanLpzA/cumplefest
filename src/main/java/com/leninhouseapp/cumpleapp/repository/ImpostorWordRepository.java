package com.leninhouseapp.cumpleapp.repository;

import com.leninhouseapp.cumpleapp.entity.ImpostorWord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImpostorWordRepository extends JpaRepository<ImpostorWord, Long> {
    List<ImpostorWord> findByUsedFalse();
}