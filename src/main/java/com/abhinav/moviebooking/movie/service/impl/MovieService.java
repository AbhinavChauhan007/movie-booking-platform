package com.abhinav.moviebooking.movie.service.impl;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;

import java.util.List;

public interface MovieService {

    MovieResponseDTO createMovie(MovieRequestDTO request);

    MovieResponseDTO updateMovie(Long movieId, MovieRequestDTO movieRequestDTO) throws MovieNotFoundException;

    MovieResponseDTO fetchMovieById(Long id) throws MovieNotFoundException;

    List<MovieResponseDTO> fetchAllMovies();

    void deleteMovie(Long id) throws MovieNotFoundException;
}
