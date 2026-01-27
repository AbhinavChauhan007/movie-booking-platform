package com.abhinav.moviebooking.booking.cache.impl;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Read-through cache implementation:
 * Cache → DB → Cache rehydration
 */
@Component
public class ReadThroughBookingCache implements BookingCache {

    private static final Duration TTL = Duration.ofHours(1);

    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final RedisTemplate<String, Booking> redisTemplate;

    public ReadThroughBookingCache(BookingPersistenceAdapter bookingPersistenceAdapter, RedisTemplate<String, Booking> redisTemplate) {
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.redisTemplate = redisTemplate;
    }

    private String key(Long bookingId) {
        return "booking:" + bookingId;
    }

    @Override
    public Optional<Booking> get(Long bookingId) {
        // 1. Cache hit
        Booking cachedBooking = redisTemplate.opsForValue().get(key(bookingId));
        if (cachedBooking != null) {
            return Optional.of(cachedBooking);
        }

        // 2. Cache miss -> DB
        Optional<Booking> bookingFromDB = bookingPersistenceAdapter.findDomainById(bookingId);

        // 3. rehydrate cache
        bookingFromDB.ifPresent(booking ->
                redisTemplate.opsForValue().set(key(bookingId), booking, TTL)
        );
        return bookingFromDB;
    }

    @Override
    public void put(Booking booking) {
        redisTemplate.opsForValue().set(key(booking.getBookingId()), booking, TTL);
    }

    @Override
    public void evict(Long bookingId) {
        redisTemplate.delete(key(bookingId));
    }
}
