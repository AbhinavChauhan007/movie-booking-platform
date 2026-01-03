package com.abhinav.moviebooking.util;

public enum ErrorCode {

    // Validations
    VALIDATION_FAILED,

    // Movie Domain
    MOVIE_NOT_FOUND,
    DUPLICATE_MOVIE,

    // Booking Domain
    SHOW_NOT_FOUND,
    SEAT_ALREADY_BOOKED,
    BOOKING_CLOSED,

    //Generic
    INTERNAL_SERVER_ERROR,

    // User related
    USER_ALREADY_EXISTS,
    USER_NOT_FOUND,
    BAD_CREDENTIALS,

    // Role related
    ROLE_NOT_FOUND


}
