package com.abhinav.moviebooking.show.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class ShowValidationException extends BusinessException {
    public ShowValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
