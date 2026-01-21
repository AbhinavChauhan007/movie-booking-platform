package com.abhinav.moviebooking.booking.exception;

public class SeatUnavailableException extends RuntimeException {

    public SeatUnavailableException(Long showId, int seatCount) {
        super("Not enough seats available for showId=" + showId +
                ", requested=" + seatCount);
    }
}
