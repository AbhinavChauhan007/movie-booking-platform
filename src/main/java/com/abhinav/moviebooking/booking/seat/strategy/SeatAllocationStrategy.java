package com.abhinav.moviebooking.booking.seat.strategy;

public interface SeatAllocationStrategy {

    /**
     * Allocate seats for a given show.
     *
     * @param showId    ID of the show
     * @param seatCount number of seats to allocate
     */
    void allocateSeats(Long showId, int seatCount);

    SeatType getSeatType();
}
