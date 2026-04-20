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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    @DisplayName("Should fetch all movies successfully with pagination")
    void fetchAllMovies_shouldReturnPageOfMovies() {
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

        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

        when(movieRepository.findAllByActiveTrue(pageable)).thenReturn(moviePage);

        // When
        Page<MovieResponseDTO> result = movieService.fetchAllMovies(pageable, null, null);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals("Inception", result.getContent().get(0).getTitle());
        assertEquals("The Matrix", result.getContent().get(1).getTitle());
        verify(movieRepository).findAllByActiveTrue(pageable);
    }

    @Test
    @DisplayName("Should fetch movies by title search with pagination")
    void fetchAllMovies_shouldReturnPageByTitleSearch() {
        // Given
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Inception");
        movie1.setGenre("Sci-Fi");
        movie1.setDurationMinutes(148);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie1), pageable, 1);

        when(movieRepository.searchByTitle("Inception", pageable)).thenReturn(moviePage);

        // When
        Page<MovieResponseDTO> result = movieService.fetchAllMovies(pageable, "Inception", null);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Inception", result.getContent().get(0).getTitle());
        verify(movieRepository).searchByTitle("Inception", pageable);
    }

    @Test
    @DisplayName("Should fetch movies by genre filter with pagination")
    void fetchAllMovies_shouldReturnPageByGenreFilter() {
        // Given
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Inception");
        movie1.setGenre("Sci-Fi");
        movie1.setDurationMinutes(148);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie1), pageable, 1);

        when(movieRepository.findByActiveTrueAndGenreIgnoreCase("Sci-Fi", pageable)).thenReturn(moviePage);

        // When
        Page<MovieResponseDTO> result = movieService.fetchAllMovies(pageable, null, "Sci-Fi");

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Sci-Fi", result.getContent().get(0).getGenre());
        verify(movieRepository).findByActiveTrueAndGenreIgnoreCase("Sci-Fi", pageable);
    }

    @Test
    @DisplayName("Should fetch movies by title search and genre filter with pagination")
    void fetchAllMovies_shouldReturnPageByTitleAndGenre() {
        // Given
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Inception");
        movie1.setGenre("Sci-Fi");
        movie1.setDurationMinutes(148);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie1), pageable, 1);

        when(movieRepository.searchByTitleAndFilterByGenre("Inception", "Sci-Fi", pageable)).thenReturn(moviePage);

        // When
        Page<MovieResponseDTO> result = movieService.fetchAllMovies(pageable, "Inception", "Sci-Fi");

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Inception", result.getContent().get(0).getTitle());
        assertEquals("Sci-Fi", result.getContent().get(0).getGenre());
        verify(movieRepository).searchByTitleAndFilterByGenre("Inception", "Sci-Fi", pageable);
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
        when(showRepository.existsByMovieIdAndActiveTrue(movieId)).thenReturn(false);
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
    @DisplayName("Should return empty page when no movies exist")
    void fetchAllMovies_shouldReturnEmptyPage_whenNoMovies() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(movieRepository.findAllByActiveTrue(pageable)).thenReturn(emptyPage);

        // When
        Page<MovieResponseDTO> result = movieService.fetchAllMovies(pageable, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(movieRepository).findAllByActiveTrue(pageable);
    }
}
