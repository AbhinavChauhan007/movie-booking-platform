package com.abhinav.moviebooking.security.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class RevokedRefreshTokenException extends BusinessException {
    public RevokedRefreshTokenException(String message) {
        super(ErrorCode.REVOKED_REFRESH_TOKEN, message);
    }

}
