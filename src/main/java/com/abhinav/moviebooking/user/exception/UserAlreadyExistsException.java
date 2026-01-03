package com.abhinav.moviebooking.user.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String message) {
        super(ErrorCode.USER_ALREADY_EXISTS, message);
    }
}
