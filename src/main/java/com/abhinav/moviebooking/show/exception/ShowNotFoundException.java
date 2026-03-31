package com.abhinav.moviebooking.show.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class ShowNotFoundException extends BusinessException {
    public ShowNotFoundException(String message) {
        super(ErrorCode.SHOW_NOT_FOUND, message);
    }
}
