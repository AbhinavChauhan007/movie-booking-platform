package com.abhinav.moviebooking.booking.seat.service;

import java.util.List;

public interface SeatService {

    /**
     * Atomically allocate specific seats for a booking.
     *
     * @return list of allocated seat IDs
     */
    List<String> allocateSeats(Long showId, int seatCount, Long bookingId);

    /**
     * Release previously allocated seats.
     */
    void releaseSeats(Long showId, List<String> seatIds);

//    boolean lockSeats(
//            String bookingId,
//            String showId,
//            List<String> seatNumbers,
//            long ttlSeconds
//    );

}
