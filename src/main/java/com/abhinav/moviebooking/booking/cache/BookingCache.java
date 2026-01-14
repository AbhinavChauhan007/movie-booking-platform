package com.abhinav.moviebooking.booking.cache;

import com.abhinav.moviebooking.booking.domain.Booking;

import java.util.Optional;

public interface BookingCache {

    Optional<Booking> get(Long bookingId);

    void put(Booking booking);

    void evict(Long bookingId);


}
