package com.abhinav.moviebooking.user.repository;

import com.abhinav.moviebooking.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllByActiveTrue();

    Optional<User> findByIdAndActiveTrue(Long id);

    Optional<User> findByUsernameAndActiveTrue(String username);

    Optional<User> findByEmailAndActiveTrue(String email);
}
