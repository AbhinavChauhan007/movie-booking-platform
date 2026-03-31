package com.abhinav.moviebooking.booking.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class BookingNotFoundException extends BusinessException {
    public BookingNotFoundException(Long bookingId) {
        super(ErrorCode.BOOKING_NOT_FOUND,"Booking with id " + bookingId + " not found");
    }
}
