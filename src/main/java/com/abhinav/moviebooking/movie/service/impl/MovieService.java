package com.abhinav.moviebooking.movie.service.impl;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieService {

    MovieResponseDTO createMovie(MovieRequestDTO request);

    MovieResponseDTO updateMovie(Long movieId, MovieRequestDTO movieRequestDTO);

    MovieResponseDTO fetchMovieById(Long id);

    Page<MovieResponseDTO> fetchAllMovies(Pageable pageable , String search, String genre);

    void deleteMovie(Long id);
}
