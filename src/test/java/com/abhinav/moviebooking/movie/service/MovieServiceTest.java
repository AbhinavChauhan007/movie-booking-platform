package com.abhinav.moviebooking.movie.service;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.movie.service.impl.MovieServiceImpl;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    private MovieRepository movieRepository;
    private MovieServiceImpl movieService;
    private ShowRepository showRepository;

    @BeforeEach
    void setUp() {
        movieRepository = mock(MovieRepository.class);
        showRepository = mock(ShowRepository.class);
        movieService = new MovieServiceImpl(movieRepository, showRepository);
    }

    @Test
    @DisplayName("Should fetch all movies successfully")
    void fetchAllMovies_shouldReturnListOfMovies() {
        // Given
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Inception");
        movie1.setGenre("Sci-Fi");
        movie1.setDurationMinutes(148);

        Movie movie2 = new Movie();
        movie2.setId(2L);
        movie2.setTitle("The Matrix");
        movie2.setGenre("Action");
        movie2.setDurationMinutes(136);

        when(movieRepository.findAllByActiveTrue()).thenReturn(List.of(movie1, movie2));

        // When
        List<MovieResponseDTO> result = movieService.fetchAllMovies();

        // Then
        assertEquals(2, result.size());
        assertEquals("Inception", result.get(0).getTitle());
        assertEquals("The Matrix", result.get(1).getTitle());
        verify(movieRepository).findAllByActiveTrue();
    }

    @Test
    @DisplayName("Should fetch movie by ID successfully")
    void fetchMovieById_shouldReturnMovie() throws MovieNotFoundException {
        // Given
        Long movieId = 1L;
        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDurationMinutes(148);

        when(movieRepository.findByIdAndActiveTrue(movieId)).thenReturn(Optional.of(movie));

        // When
        MovieResponseDTO result = movieService.fetchMovieById(movieId);

        // Then
        assertNotNull(result);
        assertEquals(movieId, result.getId());
        assertEquals("Inception", result.getTitle());
        assertEquals("Sci-Fi", result.getGenre());
        assertEquals(148, result.getDurationMinutes());
        verify(movieRepository).findByIdAndActiveTrue(movieId);
    }

    @Test
    @DisplayName("Should throw MovieNotFoundException when movie not found by ID")
    void fetchMovieById_shouldThrowException_whenMovieNotFound() {
        // Given
        Long movieId = 999L;
        when(movieRepository.findByIdAndActiveTrue(movieId)).thenReturn(Optional.empty());

        // When & Then
        MovieNotFoundException exception = assertThrows(
                MovieNotFoundException.class,
                () -> movieService.fetchMovieById(movieId)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(movieRepository).findByIdAndActiveTrue(movieId);
    }

    @Test
    @DisplayName("Should create movie successfully")
    void createMovie_shouldSaveAndReturnMovie() {
        // Given
        MovieRequestDTO requestDTO = new MovieRequestDTO();
        requestDTO.setTitle("Inception");
        requestDTO.setGenre("Sci-Fi");
        requestDTO.setDurationMinutes(148);

        Movie savedMovie = new Movie();
        savedMovie.setId(1L);
        savedMovie.setTitle("Inception");
        savedMovie.setGenre("Sci-Fi");
        savedMovie.setDurationMinutes(148);

        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // When
        MovieResponseDTO result = movieService.createMovie(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Inception", result.getTitle());
        assertEquals("Sci-Fi", result.getGenre());
        assertEquals(148, result.getDurationMinutes());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should update movie successfully")
    void updateMovie_shouldUpdateAndReturnMovie() throws MovieNotFoundException {
        // Given
        Long movieId = 1L;
        MovieRequestDTO updateDTO = new MovieRequestDTO();
        updateDTO.setTitle("Inception Updated");
        updateDTO.setGenre("Thriller");
        updateDTO.setDurationMinutes(150);

        Movie existingMovie = new Movie();
        existingMovie.setId(movieId);
        existingMovie.setTitle("Inception");
        existingMovie.setGenre("Sci-Fi");
        existingMovie.setDurationMinutes(148);

        Movie updatedMovie = new Movie();
        updatedMovie.setId(movieId);
        updatedMovie.setTitle("Inception Updated");
        updatedMovie.setGenre("Thriller");
        updatedMovie.setDurationMinutes(150);

        when(movieRepository.findByIdAndActiveTrue(movieId)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

        // When
        MovieResponseDTO result = movieService.updateMovie(movieId, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals(movieId, result.getId());
        assertEquals("Inception Updated", result.getTitle());
        assertEquals("Thriller", result.getGenre());
        assertEquals(150, result.getDurationMinutes());
        verify(movieRepository).findByIdAndActiveTrue(movieId);
        verify(movieRepository).save(existingMovie);
    }

    @Test
    @DisplayName("Should throw MovieNotFoundException when updating non-existent movie")
    void updateMovie_shouldThrowException_whenMovieNotFound() {
        // Given
        Long movieId = 999L;
        MovieRequestDTO updateDTO = new MovieRequestDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setGenre("Genre");
        updateDTO.setDurationMinutes(120);

        when(movieRepository.findByIdAndActiveTrue(movieId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                MovieNotFoundException.class,
                () -> movieService.updateMovie(movieId, updateDTO)
        );

        verify(movieRepository).findByIdAndActiveTrue(movieId);
        verify(movieRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete movie successfully")
    void deleteMovie_shouldDeleteMovie() throws MovieNotFoundException {
        // Given
        Long movieId = 1L;
        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Inception");

        when(movieRepository.findByIdAndActiveTrue(movieId)).thenReturn(Optional.of(movie));

        // When
        movieService.deleteMovie(movieId);

        // Then
        verify(movieRepository).findByIdAndActiveTrue(movieId);
        verify(movieRepository).save(movie);
        assertFalse(movie.isActive());
        assertNotNull(movie.getDeactivatedAt());
    }

    @Test
    @DisplayName("Should throw MovieNotFoundException when deleting non-existent movie")
    void deleteMovie_shouldThrowException_whenMovieNotFound() {
        // Given
        Long movieId = 999L;
        when(movieRepository.findByIdAndActiveTrue(movieId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                MovieNotFoundException.class,
                () -> movieService.deleteMovie(movieId)
        );

        verify(movieRepository).findByIdAndActiveTrue(movieId);
        verify(movieRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return empty list when no movies exist")
    void fetchAllMovies_shouldReturnEmptyList_whenNoMovies() {
        // Given
        when(movieRepository.findAllByActiveTrue()).thenReturn(List.of());

        // When
        List<MovieResponseDTO> result = movieService.fetchAllMovies();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(movieRepository).findAllByActiveTrue();
    }
}
