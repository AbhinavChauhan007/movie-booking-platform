package com.abhinav.moviebooking.booking.read;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SeatAvailabilityService {

    private final SeatAvailabilityReadService readService;


    public SeatAvailabilityService(SeatAvailabilityReadService readService) {
        this.readService = readService;
    }

    public Set<String> getAvailableSeats(Long showId, Set<String> allSeats) {
        List<String> bookedSeats = readService.getBookedSeats(showId);
        Set<String> available = new HashSet<>(allSeats);
        available.removeAll(bookedSeats);

        return available;
    }
}
