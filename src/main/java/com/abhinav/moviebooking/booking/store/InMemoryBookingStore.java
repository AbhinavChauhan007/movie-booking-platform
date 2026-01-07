package com.abhinav.moviebooking.booking.store;

import com.abhinav.moviebooking.booking.domain.Booking;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryBookingStore {

    private final Map<Long, Booking> bookingStore = new ConcurrentHashMap<>();

    public Booking create(Long bookingId) {
        Booking booking = new Booking(bookingId);
        bookingStore.computeIfAbsent(
                bookingId,
                Booking::new);
        return booking;
    }

    public Booking findById(Long bookingId) {
        Booking booking = bookingStore.get(bookingId);
        if (booking == null) {
            throw new IllegalStateException("Booking with id " + bookingId + " not found");
        }
        return booking;
    }

    public boolean exists(Long bookingId) {
        return bookingStore.containsKey(bookingId);
    }

    public Collection<Booking> findAll() {
        return bookingStore.values();
    }
}
