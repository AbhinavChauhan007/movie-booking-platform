package com.abhinav.moviebooking.util;

public enum ErrorCode {

    // Validations
    VALIDATION_FAILED("VALIDATION_FAILED", "Please Check Your Input and Try Again"),
    INVALID_ARGUMENT("INVALID_ARGUMENT", "Invalid Information Provided"),

    // Movie Domain
    MOVIE_NOT_FOUND("MOVIE_NOT_FOUND", "Movie Not Available"),
    DUPLICATE_MOVIE("DUPLICATE_MOVIE", "This Movie Already Exists"),
    MOVIE_HAS_ACTIVE_SHOWS("MOVIE_HAS_ACTIVE_SHOWS", "Movie Has Active Shows"),

    // Seat related
    INVALID_SEAT_TYPE("INVALID_SEAT_TYPE", "Invalid Seat Category Selected"),
    SEAT_ALREADY_BOOKED("SEAT_ALREADY_BOOKED", "Selected Seats Are Already Booked"),
    INVALID_SEAT_COUNT("INVALID_SEAT_COUNT", "Invalid Number of Seats Selected"),
    SEAT_UNAVAILABLE("SEAT_UNAVAILABLE", "Requested Seats Are Not Available"),

    // Payment related
    PAYMENT_FAILED("PAYMENT_FAILED", "Payment Could Not Be Processed"),
    PAYMENT_TIMEOUT("PAYMENT_TIMEOUT", "Payment Request Timed Out"),
    PAYMENT_INTERRUPTED("PAYMENT_INTERRUPTED", "Payment Process Was Interrupted"),

    // Booking Domain
    BOOKING_CLOSED("BOOKING_CLOSED", "Booking Is No Longer Available"),
    BOOKING_NOT_FOUND("BOOKING_NOT_FOUND", "Booking Could Not Be Found"),
    BOOKING_CONFLICT("BOOKING_CONFLICT", "You Already Have a Booking for This Show"),
    INVALID_BOOKING_STATE("INVALID_BOOKING_STATE", "This Action Cannot Be Performed on Your Booking"),

    // Authentication & Authorization
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Incorrect Email or Password"),
    JWT_AUTHENTICATION_FAILED("JWT_AUTHENTICATION_FAILED", "Login Session Could Not Be Verified"),
    INVALID_TOKEN("INVALID_TOKEN", "Your Session Is Invalid"),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "Your Session Has Expired - Please Login Again"),
    INVALID_AUTH_HEADER("INVALID_AUTH_HEADER", "Authentication Information Missing"),
    ACCESS_DENIED("ACCESS_DENIED", "You Don't Have Permission to Access This"),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Session Token Is Invalid"),
    REVOKED_REFRESH_TOKEN("REVOKED_REFRESH_TOKEN", "Your Session Has Been Logged Out"),
    EXPIRED_REFRESH_TOKEN("EXPIRED_REFRESH_TOKEN", "Session Expired - Please Login Again"),

    // User related
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "An Account with This Email Already Exists"),
    USER_NOT_FOUND("USER_NOT_FOUND", "User Account Not Found"),
    USERNAME_NOT_FOUND("USERNAME_NOT_FOUND", "No Account Found with This Email"),
    USER_HAS_ACTIVE_BOOKINGS("USER_HAS_ACTIVE_BOOKINGS", "Cannot Delete Account with Active Bookings"),

    // Role related
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "User Role Not Found"),
    INVALID_ROLE_FORMAT("INVALID_ROLE_FORMAT", "Invalid Role Configuration"),
    DUPLICATE_ROLE("DUPLICATE_ROLE", "This Role Already Exists for the User"),

    // Show related
    INVALID_SHOW_TIME("INVALID_SHOW_TIME", "Show Time Must Be in the Future"),
    PAST_SHOW_BOOKING("PAST_SHOW_BOOKING", "Cannot Book Tickets for Past Shows"),
    SHOW_NOT_FOUND("SHOW_NOT_FOUND", "Show Not Available"),
    SHOW_ALREADY_BOOKED("SHOW_ALREADY_BOOKED", "Show Has Existing Bookings"),

    // Generic
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested Item Could Not Be Found"),
    ILLEGAL_STATE("ILLEGAL_STATE", "This Operation Cannot Be Performed Right Now"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Oops! Something Went Wrong on Our End");

    private final String code;
    private final String title;

    ErrorCode(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

}
