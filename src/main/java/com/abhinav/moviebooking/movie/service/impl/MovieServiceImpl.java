package com.abhinav.moviebooking.movie.service.impl;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.exception.MovieHasActiveShowsException;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;

    public MovieServiceImpl(MovieRepository movieRepository, ShowRepository showRepository) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
    }

    //fetch All Movies
    public List<MovieResponseDTO> fetchAllMovies() {
        return movieRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // fetch movie By ID
    public MovieResponseDTO fetchMovieById(Long id) {
        Movie fetchedMovie = movieRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return mapToResponseDTO(fetchedMovie);

    }

    // save movie
    public MovieResponseDTO createMovie(MovieRequestDTO movieRequestDTO) {
        Movie movie = mapToEntity(movieRequestDTO);
        Movie savedMovie = movieRepository.save(movie);
        return mapToResponseDTO(savedMovie);
    }

    //updateMovie with field-level validation
    public MovieResponseDTO updateMovie(Long id, MovieRequestDTO updateMovieRequestDTO) {
        Movie existingMovie = movieRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        // ✅ ALWAYS allow safe display-only updates (title, genre)
        existingMovie.setTitle(updateMovieRequestDTO.getTitle());
        existingMovie.setGenre(updateMovieRequestDTO.getGenre());

        // ⚠️ BLOCK duration changes if active shows exist
        if (updateMovieRequestDTO.getDurationMinutes() != existingMovie.getDurationMinutes()) {
            List<Show> activeShows = showRepository.findByMovieIdAndActiveTrue(id);

            if (!activeShows.isEmpty()) {
                throw new MovieHasActiveShowsException(
                        "Cannot change movie duration. Movie has " + activeShows.size() +
                                " active show(s). Show scheduling is based on current duration (" +
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

    //delete Movie (soft delete)
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
        movie.setTitle(movieRequestDTO.getTitle());
        movie.setGenre(movieRequestDTO.getGenre());
        movie.setDurationMinutes(movieRequestDTO.getDurationMinutes());
        return movie;
    }


}
