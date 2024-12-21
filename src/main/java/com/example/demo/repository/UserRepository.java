package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPasswordResetToken(String token);
}
