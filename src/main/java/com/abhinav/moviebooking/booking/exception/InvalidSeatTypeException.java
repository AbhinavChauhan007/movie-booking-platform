package com.abhinav.moviebooking.booking.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class InvalidSeatTypeException extends BusinessException {
    public InvalidSeatTypeException(String seatType) {
        super(ErrorCode.INVALID_SEAT_TYPE,
                "No seat allocation strategy found for type: " + seatType);
    }
}
