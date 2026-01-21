package com.abhinav.moviebooking.booking.seat.strategy.impl;

import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirstAvailableSeatStrategy implements SeatAllocationStrategy {


    @Override
    public List<String> selectSeats(Long showId, int seatCount) {
        // placeholder: Redis will enforce actual availability
        return List.of();
    }

    @Override
    public SeatType getSeatType() {
        return SeatType.FIRST_AVAILABLE;
    }
}
