package com.abhinav.moviebooking.security.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class ExpiredRefreshTokenException extends BusinessException {
    public ExpiredRefreshTokenException(String message) {
        super(ErrorCode.EXPIRED_REFRESH_TOKEN, message);
    }
}
