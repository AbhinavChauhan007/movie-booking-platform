package com.abhinav.moviebooking.show.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class ShowAlreadyBookedException extends BusinessException {
    public ShowAlreadyBookedException(String message) {
        super(ErrorCode.SHOW_ALREADY_BOOKED, message);
    }
}
