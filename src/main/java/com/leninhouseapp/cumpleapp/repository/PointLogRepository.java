package com.leninhouseapp.cumpleapp.repository;

import com.leninhouseapp.cumpleapp.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    List<PointLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
