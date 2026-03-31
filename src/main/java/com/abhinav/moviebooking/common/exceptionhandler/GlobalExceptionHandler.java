package com.abhinav.moviebooking.common.exceptionhandler;

import com.abhinav.moviebooking.booking.exception.BookingConcurrencyException;
import com.abhinav.moviebooking.common.dto.ApiResponse;
import com.abhinav.moviebooking.common.dto.error.ApiErrorResponse;
import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.movie.exception.MovieHasActiveShowsException;
import com.abhinav.moviebooking.security.exception.JwtAuthenticationException;
import com.abhinav.moviebooking.util.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the movie booking application.
 * <p>
 * Handles all exceptions thrown by controllers and provides consistent error responses.
 * Uses smart HTTP status mapping based on ErrorCode for BusinessException subclasses.
 *
 * @author Abhinav Chauhan
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== VALIDATION EXCEPTIONS ====================

    /**
     * Handles Bean Validation failures from @Valid annotations.
     * Returns 400 BAD_REQUEST with field-level error details.
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        log.warn("Validation failed for request: {} - Fields: {}", request.getRequestURI(), fieldErrors);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                ErrorCode.VALIDATION_FAILED,
                "Invalid request parameters",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles IllegalArgumentException for generic invalid arguments.
     * Returns 400 BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Invalid argument: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_ARGUMENT,
                "Invalid Argument",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ==================== AUTHENTICATION & AUTHORIZATION EXCEPTIONS ====================

    /**
     * Handles Spring Security UsernameNotFoundException during authentication.
     * Returns 401 UNAUTHORIZED.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Username not found: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.USERNAME_NOT_FOUND,
                "Authentication Failed",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Handles Spring Security BadCredentialsException for invalid login attempts.
     * Returns 401 UNAUTHORIZED.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Bad credentials attempt from: {}", request.getRemoteAddr());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.BAD_CREDENTIALS,
                "Authentication Failed",
                "Invalid email or password",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Handles custom JwtAuthenticationException for JWT-related authentication failures.
     * Returns 401 UNAUTHORIZED.
     * Note: This extends RuntimeException, not BusinessException.
     */
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtAuthenticationException(
            JwtAuthenticationException ex,
            HttpServletRequest request) {

        log.warn("JWT authentication failed: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.JWT_AUTHENTICATION_FAILED,
                "JWT Authentication Failed",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Handles Spring Security AccessDeniedException for authorization failures.
     * Returns 403 FORBIDDEN.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied for path: {} - User: {}", request.getRequestURI(), request.getRemoteUser());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.ACCESS_DENIED,
                "Access Denied",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    // ==================== BUSINESS DOMAIN EXCEPTIONS ====================

    /**
     * Smart handler for all BusinessException subclasses.
     * <p>
     * Dynamically maps ErrorCode to appropriate HTTP status:
     * - *_NOT_FOUND → 404 NOT_FOUND
     * - *_ALREADY_EXISTS, *_CONFLICT, *_UNAVAILABLE → 409 CONFLICT
     * - *_TOKEN, *_CREDENTIALS, *_AUTH_* → 401 UNAUTHORIZED
     * - Everything else → 400 BAD_REQUEST
     * <p>
     * This single handler replaces 15+ dedicated handlers.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus httpStatus = mapErrorCodeToHttpStatus(errorCode);

        log.warn("Business exception: {} - ErrorCode: {} - HttpStatus: {}",
                ex.getMessage(), errorCode, httpStatus);

        ApiErrorResponse response = new ApiErrorResponse(
                httpStatus.value(),
                errorCode,
                errorCode.getTitle(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    /**
     * Handles BookingConcurrencyException for optimistic locking failures.
     * Returns 409 CONFLICT.
     * <p>
     * Note: This extends RuntimeException, not BusinessException.
     */
    @ExceptionHandler(BookingConcurrencyException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingConcurrency(
            BookingConcurrencyException ex,
            HttpServletRequest request) {

        log.warn("Booking concurrency conflict: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                ErrorCode.BOOKING_CONFLICT,
                "Booking Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    // ==================== SYSTEM EXCEPTIONS ====================

    /**
     * Handles IllegalStateException for programming errors and invariant violations.
     * Returns 500 INTERNAL_SERVER_ERROR.
     * <p>
     * These should never happen in production and indicate a bug.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.error("Illegal state encountered: {}", ex.getMessage(), ex);

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.ILLEGAL_STATE,
                "Illegal State",
                "An unexpected state was encountered. Please contact support.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Catch-all handler for any unhandled exceptions.
     * Returns 500 INTERNAL_SERVER_ERROR.
     * <p>
     * This is the last line of defense and should rarely be triggered.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please contact support if the issue persists.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }


    // ==================== HELPER METHODS ====================

    /**
     * Maps ErrorCode to appropriate HTTP status code.
     *
     * @param errorCode the error code from BusinessException
     * @return appropriate HTTP status
     */
    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }

        String code = errorCode.name();

        // 404 NOT_FOUND - Resource not found errors
        if (code.endsWith("_NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }

        // 409 CONFLICT - Resource already exists or conflict errors
        if (code.contains("_ALREADY_EXISTS") ||
                code.contains("_CONFLICT") ||
                code.contains("_UNAVAILABLE") ||
                code.equals("DUPLICATE_ROLE") ||
                code.equals("SEAT_ALREADY_BOOKED") ||
                code.equals("SHOW_ALREADY_BOOKED") ||
                code.equals("MOVIE_HAS_ACTIVE_SHOWS")) {
            return HttpStatus.CONFLICT;
        }

        // 401 UNAUTHORIZED - Authentication/token errors
        if (code.contains("_TOKEN") ||
                code.contains("_CREDENTIALS") ||
                code.contains("_AUTH") ||
                code.equals("BAD_CREDENTIALS")) {
            return HttpStatus.UNAUTHORIZED;
        }

        // 400 BAD_REQUEST - Default for validation and business rule violations
        return HttpStatus.BAD_REQUEST;
    }


}