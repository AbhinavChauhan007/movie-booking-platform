package com.abhinav.moviebooking.booking.persistence.adapter;

import com.abhinav.moviebooking.booking.persistence.entity.SeatBookingEntity;
import com.abhinav.moviebooking.booking.persistence.repository.SeatBookingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeatBookingPersistenceAdapter {

    private final SeatBookingRepository seatBookingRepository;

    public SeatBookingPersistenceAdapter(SeatBookingRepository seatBookingRepository) {
        this.seatBookingRepository = seatBookingRepository;
    }

    public void saveSeats(Long bookingId, Long showId, List<String> seats) {
        for (String seat : seats) {
            seatBookingRepository.save(
                    new SeatBookingEntity(seat, showId, bookingId)
            );
        }
    }
}
