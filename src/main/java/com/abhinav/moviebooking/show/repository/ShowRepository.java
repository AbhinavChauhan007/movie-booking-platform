package com.abhinav.moviebooking.show.repository;

import com.abhinav.moviebooking.show.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    Optional<Show> findByIdAndActiveTrue(Long id);

    // New paginated methods
    Page<Show> findAllByActiveTrue(Pageable pageable);

    // Filter by movieId
    Page<Show> findByMovieIdAndActiveTrue(Long movieId, Pageable pageable);

    // Filter by date range
    @Query("SELECT s FROM Show s WHERE s.active = true " +
            "AND s.startTime >= :startDate AND s.startTime <= :endDate")
    Page<Show> findByDateRange(@Param("startDate") Instant startDate,
                               @Param("endDate") Instant endDate,
                               Pageable pageable);

    // Filter by movieId and date range
    @Query("SELECT s FROM Show s WHERE s.active = true " +
            "AND s.movieId = :movieId " +
            "AND s.startTime >= :startDate AND s.startTime <= :endDate")
    Page<Show> findByMovieIdAndDateRange(@Param("movieId") Long movieId,
                                         @Param("startDate") Instant startDate,
                                         @Param("endDate") Instant endDate,
                                         Pageable pageable);

    // Future shows (paginated)
    @Query("SELECT s FROM Show s WHERE s.active = true AND s.startTime > :now")
    Page<Show> findFutureShows(@Param("now") Instant now, Pageable pageable);

    // Future shows by movieId (paginated)
    @Query("SELECT s FROM Show s WHERE s.active = true " +
            "AND s.movieId = :movieId AND s.startTime > :now")
    Page<Show> findFutureShowsByMovieId(@Param("movieId") Long movieId,
                                        @Param("now") Instant now,
                                        Pageable pageable);

    // Filter by screen number
    Page<Show> findByActiveTrueAndScreenNumber(Integer screenNumber, Pageable pageable);

    boolean existsByMovieIdAndActiveTrue(Long movieId);
}
