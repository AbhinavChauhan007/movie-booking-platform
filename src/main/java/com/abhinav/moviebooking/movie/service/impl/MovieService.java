package com.abhinav.moviebooking.movie.service.impl;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;

import java.util.List;

public interface MovieService {

    MovieResponseDTO createMovie(MovieRequestDTO request);

    MovieResponseDTO updateMovie(Long movieId, MovieRequestDTO movieRequestDTO);

    MovieResponseDTO fetchMovieById(Long id);

    List<MovieResponseDTO> fetchAllMovies();

    void deleteMovie(Long id);
}
