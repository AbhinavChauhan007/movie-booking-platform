package com.abhinav.moviebooking.booking.seat;

public interface SeatAllocationStrategy {

    /**
     * Allocate seats for a given show.
     *
     * @param showId    ID of the show
     * @param seatCount number of seats to allocate
     */
    void allocateSeats(Long showId, int seatCount);

    void releaseSeats(Long showId, int seatCount);


    SeatType getSeatType();
}
