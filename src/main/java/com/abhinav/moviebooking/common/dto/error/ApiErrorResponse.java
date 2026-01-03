package com.abhinav.moviebooking.common.dto.error;

import com.abhinav.moviebooking.util.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private LocalDateTime timeStamp;

    private int status;

    private String error;

    private String message;

    private String path;

    // OPTIONAL
    private Map<String, String> fieldErrors;

    private ErrorCode errorCode;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(int status, String error, String message, String path) {
        this.timeStamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ApiErrorResponse(int status, ErrorCode errorCode, String error, String message, String path
    ) {
        this.timeStamp = LocalDateTime.now();
        this.status = status;
        this.errorCode = errorCode;
        this.error = error;
        this.message = message;
        this.path = path;
    }


    public ApiErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this.timeStamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public ApiErrorResponse(int status, String error, ErrorCode errorCode, String message, String path, Map<String, String> fieldErrors) {
        this.timeStamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
