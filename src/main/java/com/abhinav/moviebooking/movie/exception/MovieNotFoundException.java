package com.abhinav.moviebooking.movie.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class MovieNotFoundException extends BusinessException {
    public MovieNotFoundException(Long movieId) {
        super(
                ErrorCode.MOVIE_NOT_FOUND,
                "Movie not found with id: " + movieId
        );
    }
}
