package com.abhinav.moviebooking.show.repository;

import com.abhinav.moviebooking.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show,Long> {

    @Query("""
        SELECT s FROM Show s
        WHERE s.startTime > :now
    """)
    List<Show> findAllFutureShows(Instant now);
}
