package com.abhinav.moviebooking.security.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class ExpiredTokenException extends BusinessException {
    public ExpiredTokenException(String message) {
        super(ErrorCode.EXPIRED_TOKEN, message);
    }
}
