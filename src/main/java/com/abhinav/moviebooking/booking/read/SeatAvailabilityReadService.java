package com.abhinav.moviebooking.booking.read;

import com.abhinav.moviebooking.booking.persistence.repository.SeatBookingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeatAvailabilityReadService {

    private final SeatBookingRepository seatBookingRepository;


    public SeatAvailabilityReadService(SeatBookingRepository seatBookingRepository) {
        this.seatBookingRepository = seatBookingRepository;
    }


    public List<String> getBookedSeats(Long showId) {
        return seatBookingRepository.findBookedSeats(showId);
    }
}
