package com.abhinav.moviebooking.booking.read;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BookingReadService {

    private final BookingCache bookingCache;

    public BookingReadService(BookingCache cache) {
        this.bookingCache = cache;
    }

    public Booking getBooking(Long bookingId) {
        return bookingCache.get(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

    }


}
