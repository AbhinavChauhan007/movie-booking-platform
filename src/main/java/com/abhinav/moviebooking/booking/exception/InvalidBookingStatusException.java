package com.abhinav.moviebooking.booking.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class InvalidBookingStatusException extends BusinessException {
    public InvalidBookingStatusException(String message) {
        super(ErrorCode.INVALID_BOOKING_STATUS, message);
    }
}
