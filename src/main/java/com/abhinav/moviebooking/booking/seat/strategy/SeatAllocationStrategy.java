package com.abhinav.moviebooking.booking.seat;

import java.util.List;

public interface SeatAllocationStrategy {

    /**
     * Allocate seats for a given show.
     *
     * @param showId    ID of the show
     * @param seatCount number of seats to allocate
     */
    List<String> selectSeats(
            Long showId,
            int seatCount
    );

    SeatType getSeatType();
}
