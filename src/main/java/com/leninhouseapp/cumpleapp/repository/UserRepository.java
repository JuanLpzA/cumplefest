package com.leninhouseapp.cumpleapp.repository;

import com.leninhouseapp.cumpleapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<com.leninhouseapp.cumpleapp.entity.User> findByUsername(String username);
    List<User> findByActiveTrue();
    List<User> findByActiveTrueAndRoleNot(User.Role role);
    List<User> findByActiveTrueOrderByPointsDesc();
}
