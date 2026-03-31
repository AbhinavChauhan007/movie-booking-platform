package com.abhinav.moviebooking.movie.controller;

import com.abhinav.moviebooking.common.dto.ApiResponse;
import com.abhinav.moviebooking.movie.dto.request.MovieRequestDTO;
import com.abhinav.moviebooking.movie.dto.response.MovieResponseDTO;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;
import com.abhinav.moviebooking.movie.service.impl.MovieServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movies", description = "Movie catalog management")
public class MovieController {

    private final MovieServiceImpl movieService;

    public MovieController(MovieServiceImpl movieService) {
        this.movieService = movieService;
    }

    // GET /api/movies/getAllMovies -> fetch All movies
    // Everyone
    @GetMapping("/getAllMovies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(
            summary = "Get all movies",
            description = "Retrieve complete list of all movies in the catalog"
    )
    public ResponseEntity<ApiResponse<List<MovieResponseDTO>>> fetchAllMovies() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Movies retrieved successfully", movieService.fetchAllMovies()
                )
        );
    }

    // GET /api/movies/getMovie/{id} -> fetch movie by ID
    // Everyone
    @GetMapping("/getMovie/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(
            summary = "Get movie by ID",
            description = "Retrieve detailed information about a specific movie"
    )
    public ResponseEntity<ApiResponse<MovieResponseDTO>> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Movie retrieved successfully", movieService.fetchMovieById(id)
                )
        );
    }

    // POST /api/movies/saveMovie -> save a new Movie
    // ADMIN only
    @PostMapping("/createMovie")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Create movie (Admin only)",
            description = "Add a new movie to the catalog with title, genre, and duration"
    )
    public ResponseEntity<ApiResponse<MovieResponseDTO>> createMovie(@Valid @RequestBody MovieRequestDTO movieRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie created successfully", movieService.createMovie(movieRequestDTO)));

    }

    // PUT /api/movies/updateMovie/{id} -> update and existing movie
    // ADMIN only
    @PutMapping("/updateMovie/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Update movie (Admin only)",
            description = "Update existing movie details"
    )
    public ResponseEntity<ApiResponse<MovieResponseDTO>> updateMovie(@PathVariable Long id, @RequestBody @Valid MovieRequestDTO updatedMovieRequestDTO)  {
        return ResponseEntity.ok(
                ApiResponse.success("Movie updated successfully", movieService.updateMovie(id, updatedMovieRequestDTO))
        );
    }

    // DELETE /api/movies/deleteMovie/{id} -> delete an existing movie
    // ADMIN only
    @DeleteMapping("/deleteMovie/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Delete movie (Admin only)",
            description = "Remove a movie from the catalog"
    )
    public ResponseEntity<ApiResponse<MovieResponseDTO>> deleteMovie(@PathVariable Long movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.ok(
                ApiResponse.success("Movie with ID " + movieId + " deleted successfully")
        );
    }


}
