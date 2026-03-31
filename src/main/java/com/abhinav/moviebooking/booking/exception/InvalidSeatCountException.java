package com.abhinav.moviebooking.booking.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class InvalidSeatCountException extends BusinessException {
    public InvalidSeatCountException(String message) {
        super(ErrorCode.INVALID_SEAT_COUNT,message);
    }
}
