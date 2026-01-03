package com.abhinav.moviebooking.user.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND,
                "User not found with Id : " + userId);
    }
}
