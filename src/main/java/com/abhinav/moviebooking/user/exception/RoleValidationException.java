package com.abhinav.moviebooking.user.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class RoleValidationException extends BusinessException {

    public RoleValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
