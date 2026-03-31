package com.abhinav.moviebooking.booking.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;

import static com.abhinav.moviebooking.util.ErrorCode.INVALID_BOOKING_STATE;

public class InvalidBookingStateException extends BusinessException {
    public InvalidBookingStateException(String message) {
        super(INVALID_BOOKING_STATE,message);
    }
}
