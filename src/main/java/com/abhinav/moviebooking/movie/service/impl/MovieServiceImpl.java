package com.abhinav.moviebooking.movie.service.impl;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    //fetch All Movies
    public List<MovieResponseDTO> fetchAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // fetch movie By ID
    public MovieResponseDTO fetchMovieById(Long id) {
        Movie fetchedMovie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return mapToResponseDTO(fetchedMovie);

    }

    // save movie
    public MovieResponseDTO createMovie(MovieRequestDTO movieRequestDTO) {
        Movie movie = mapToEntity(movieRequestDTO);
        Movie savedMovie = movieRepository.save(movie);
        return mapToResponseDTO(savedMovie);
    }

    //updateMovie
    public MovieResponseDTO updateMovie(Long id, MovieRequestDTO updateMovieRequestDTO) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        existingMovie.setGenre(updateMovieRequestDTO.getGenre());
        existingMovie.setTitle(updateMovieRequestDTO.getTitle());
        existingMovie.setDurationMinutes(updateMovieRequestDTO.getDurationMinutes());
        Movie updatedMovie = movieRepository.save(existingMovie);
        return mapToResponseDTO(updatedMovie);

    }

    //delete Movie
    public void deleteMovie(Long id) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        movieRepository.deleteById(id);
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
