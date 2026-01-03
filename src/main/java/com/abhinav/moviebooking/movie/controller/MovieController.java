package com.abhinav.moviebooking.movie.controller;

import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import com.abhinav.moviebooking.movie.service.impl.MovieServiceImpl;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieServiceImpl movieService;

    public MovieController(MovieServiceImpl movieService) {
        this.movieService = movieService;
    }

    // GET /api/movies/getAllMovies -> fetch All movies
    // Everyone
    @GetMapping("/getAllMovies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<MovieResponseDTO> fetchAllMovies() {
        return movieService.fetchAllMovies();
    }

    // GET /api/movies/getMovie/{id} -> fetch movie by ID
    // Everyone
    @GetMapping("/getMovie/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public MovieResponseDTO getMovieById(@PathVariable Long id) {
        return movieService.fetchMovieById(id);
    }

    // POST /api/movies/saveMovie -> save a new Movie
    // ADMIN only
    @PostMapping("/saveMovie")
    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponseDTO addMovie(@Valid @RequestBody MovieRequestDTO movieRequestDTO) {
        System.out.println("Received movieRequestDTO: "
                + movieRequestDTO.getTitle() + ", "
                + movieRequestDTO.getGenre() + ", "
                + movieRequestDTO.getDurationMinutes());
        return movieService.createMovie(movieRequestDTO);
    }

    // PUT /api/movies/updateMovie/{id} -> update and existing movie
    // ADMIN only
    @PutMapping("/updateMovie/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponseDTO updateMovie(@PathVariable Long id, @RequestBody @Valid MovieRequestDTO updatedMovieRequestDTO) {
        return movieService.updateMovie(id, updatedMovieRequestDTO);
    }

    // DELETE /api/movies/deleteMovie/{id} -> delete an existing movie
    // ADMIN only
    @DeleteMapping("/deleteMovie/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }


}
