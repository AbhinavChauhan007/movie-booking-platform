package com.abhinav.moviebooking.movie.service.impl;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.exception.MovieHasActiveShowsException;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;

    public MovieServiceImpl(MovieRepository movieRepository, ShowRepository showRepository) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
    }

    // fetch movie By ID
    public MovieResponseDTO fetchMovieById(Long id) {
        Movie fetchedMovie = movieRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return mapToResponseDTO(fetchedMovie);

    }

    // save movie
    @Transactional
    public MovieResponseDTO createMovie(MovieRequestDTO movieRequestDTO) {
        Movie movie = mapToEntity(movieRequestDTO);
        Movie savedMovie = movieRepository.save(movie);
        return mapToResponseDTO(savedMovie);
    }

    //updateMovie with field-level validation
    @Transactional
    public MovieResponseDTO updateMovie(Long id, MovieRequestDTO updateMovieRequestDTO) {
        Movie existingMovie = movieRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        // ✅ ALWAYS allow safe display-only updates (title, genre)
        existingMovie.setTitle(updateMovieRequestDTO.getTitle().trim());
        existingMovie.setGenre(updateMovieRequestDTO.getGenre().trim());

        // ⚠️ BLOCK duration changes if active shows exist
        if (updateMovieRequestDTO.getDurationMinutes() != existingMovie.getDurationMinutes()) {
            if (showRepository.existsByMovieIdAndActiveTrue(id)) {
                throw new MovieHasActiveShowsException(
                        "Cannot change movie duration. Movie has active shows. " +
                                "Show scheduling is based on current duration (" +
                                existingMovie.getDurationMinutes() + " minutes). " +
                                "Please deactivate all shows first before changing duration."
                );
            }

            // Safe to update duration - no active shows
            existingMovie.setDurationMinutes(updateMovieRequestDTO.getDurationMinutes());
        }

        Movie updatedMovie = movieRepository.save(existingMovie);
        return mapToResponseDTO(updatedMovie);
    }

    // New paginated fetch with search and filters
    @Override
    public Page<MovieResponseDTO> fetchAllMovies(Pageable pageable, String search, String genre) {
        Page<Movie> moviePage;

        if (search != null && !search.trim().isEmpty() && genre != null && !genre.trim().isEmpty()) {
            // Both search and genre provided - search first, then filter by genre
            moviePage = movieRepository.searchByTitleAndFilterByGenre(
                    search.trim(), genre.trim(), pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            // Only search provided
            moviePage = movieRepository.searchByTitle(search.trim(), pageable);
        } else if (genre != null && !genre.trim().isEmpty()) {
            // Only genre filter provided
            moviePage = movieRepository.findByActiveTrueAndGenreIgnoreCase(genre.trim(), pageable);
        } else {
            // No filters - return all
            moviePage = movieRepository.findAllByActiveTrue(pageable);
        }

        // Map entities to DTOs
        return moviePage.map(this::mapToResponseDTO);
    }

    //delete Movie (soft delete)
    @Transactional
    public void deleteMovie(Long id) {
        Movie existingMovie = movieRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        existingMovie.setActive(false);
        existingMovie.setDeactivatedAt(Instant.now());
        movieRepository.save(existingMovie);
    }


    // helper functions for mapping
    private MovieResponseDTO mapToResponseDTO(Movie movie) {
        return new MovieResponseDTO(movie.getId(), movie.getTitle(), movie.getGenre(), movie.getDurationMinutes());
    }

    private Movie mapToEntity(MovieRequestDTO movieRequestDTO) {
        Movie movie = new Movie();
        movie.setTitle(movieRequestDTO.getTitle().trim());
        movie.setGenre(movieRequestDTO.getGenre().trim());
        movie.setDurationMinutes(movieRequestDTO.getDurationMinutes());
        return movie;
    }


}
