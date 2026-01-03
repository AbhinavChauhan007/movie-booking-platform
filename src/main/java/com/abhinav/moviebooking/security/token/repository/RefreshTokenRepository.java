package com.abhinav.moviebooking.security.token.repository;

import com.abhinav.moviebooking.security.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUsernameAndRevokedFalse(String username);
}
