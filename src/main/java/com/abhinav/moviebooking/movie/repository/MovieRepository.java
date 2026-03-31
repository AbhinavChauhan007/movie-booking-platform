package com.abhinav.moviebooking.movie.repository;

import com.abhinav.moviebooking.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByActiveTrue();

    Optional<Movie> findByIdAndActiveTrue(Long id);


}
