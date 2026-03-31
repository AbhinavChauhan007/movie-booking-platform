package com.abhinav.moviebooking.movie.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class MovieHasActiveShowsException extends BusinessException {
    public MovieHasActiveShowsException(String message) {
        super(ErrorCode.MOVIE_HAS_ACTIVE_SHOWS, message);
    }
}
