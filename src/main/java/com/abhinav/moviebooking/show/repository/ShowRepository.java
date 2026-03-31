package com.abhinav.moviebooking.show.repository;

import com.abhinav.moviebooking.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("""
                SELECT s FROM Show s
                WHERE s.startTime > :now
                AND s.active = true
            """)
    List<Show> findAllFutureShows(Instant now);

    List<Show> findAllByActiveTrue();

    Optional<Show> findByIdAndActiveTrue(Long id);

    List<Show> findByMovieIdAndActiveTrue(Long movieId);
}
