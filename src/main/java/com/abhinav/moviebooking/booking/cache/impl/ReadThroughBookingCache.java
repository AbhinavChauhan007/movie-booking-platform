package com.abhinav.moviebooking.booking.cache.impl;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Read-through cache implementation:
 * Cache → DB → Cache rehydration
 */
@Component
public class ReadThroughBookingCache implements BookingCache {

    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final Map<Long, Booking> cache = new ConcurrentHashMap<>();

    public ReadThroughBookingCache(BookingPersistenceAdapter bookingPersistenceAdapter) {
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
    }

    @Override
    public Optional<Booking> get(Long bookingId) {
        // 1. Cache hit
        Booking cachedBooking = cache.get(bookingId);
        if (cachedBooking != null) {
            return Optional.of(cachedBooking);
        }

        // 2. Cache miss -> DB
        Optional<Booking> bookingFromDB = bookingPersistenceAdapter.findDomainById(bookingId);

        // 3. rehydrate cache
        bookingFromDB.ifPresent(booking ->
                cache.put(bookingId, booking)
        );
        return bookingFromDB;
    }

    @Override
    public void put(Booking booking) {
        cache.put(booking.getBookingId(), booking);
    }

    @Override
    public void evict(Long bookingId) {
        cache.remove(bookingId);
    }
}
