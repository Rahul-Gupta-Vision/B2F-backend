package com.example.B2F.repository;

import com.example.B2F.entities.RefreshTokens;
import com.example.B2F.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, Long> {
    Optional<RefreshTokens> findByToken(String token);
    Optional<RefreshTokens> findByUser(User user);
}
