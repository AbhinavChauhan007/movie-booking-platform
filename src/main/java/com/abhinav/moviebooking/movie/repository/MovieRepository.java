package com.abhinav.moviebooking.movie.repository;

import com.abhinav.moviebooking.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Find single movie by ID (active only)
    Optional<Movie> findByIdAndActiveTrue(Long id);

    // Find all active movies (paginated, no filters)
    Page<Movie> findAllByActiveTrue(Pageable pageable);

    // Filter by genre only
    Page<Movie> findByActiveTrueAndGenreIgnoreCase(String genre, Pageable pageable);

    // Search by title only (case-insensitive, partial match)
    @Query("SELECT m FROM Movie m WHERE m.active = true " +
            "AND LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Movie> searchByTitle(@Param("search") String search, Pageable pageable);

    // Search by title AND filter by genre (combined)
    @Query("SELECT m FROM Movie m WHERE m.active = true " +
            "AND LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "AND LOWER(m.genre) = LOWER(:genre)")
    Page<Movie> searchByTitleAndFilterByGenre(
            @Param("search") String search,
            @Param("genre") String genre,
            Pageable pageable
    );

}
