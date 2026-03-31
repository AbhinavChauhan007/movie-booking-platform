package com.abhinav.moviebooking.security.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class InvalidAuthorizationHeaderException extends BusinessException {
    public InvalidAuthorizationHeaderException(String message) {
        super(ErrorCode.INVALID_AUTH_HEADER, message);
    }
}
