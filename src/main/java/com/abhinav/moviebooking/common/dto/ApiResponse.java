package com.abhinav.moviebooking.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Standardized API response wrapper for successful operations.
 * All controller endpoints should return this structure for success cases.
 *
 * For error responses, see ApiErrorResponse which is used by GlobalExceptionHandler.
 *
 * @param <T> The type of data being returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Instant timestamp;

    /**
     * Default constructor for deserialization
     */
    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    /**
     * Private constructor for factory methods
     */
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Create a successful response with data
     *
     * @param message Success message
     * @param data Response data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Create a successful response without data
     *
     * @param message Success message
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // ==================== GETTERS AND SETTERS ====================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}