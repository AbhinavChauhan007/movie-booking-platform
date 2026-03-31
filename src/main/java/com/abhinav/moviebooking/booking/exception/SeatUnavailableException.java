package com.abhinav.moviebooking.booking.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class SeatUnavailableException extends BusinessException {

    public SeatUnavailableException(Long showId, int seatCount) {
        super(ErrorCode.SEAT_UNAVAILABLE,
                "Not enough seats available for showId=" + showId +
                        ", requested=" + seatCount);
    }

}
