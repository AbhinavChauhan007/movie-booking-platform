package com.abhinav.moviebooking.booking.seat.service.impl;

import com.abhinav.moviebooking.booking.seat.service.SeatService;
import com.abhinav.moviebooking.booking.exception.SeatUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.List;

@Service
public class RedisSeatService implements SeatService {

    private static final String SHOW_SEATS_KEY = "show:%d:available_seats";
    private static final String BOOKING_SEATS_KEY = "booking:%s:locked_seats";
    private static final int BOOKING_TTL_SECONDS = 600; // 10 minutes

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<List> allocateScript;

    public RedisSeatService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.allocateScript = RedisScript.of(
                new ClassPathResource("lua/allocate_seats.lua"),
                List.class
        );
    }


    @Override
    public List<String> allocateSeats(Long showId, int seatCount, Long bookingId) {
        String showKey = String.format(SHOW_SEATS_KEY, showId);
        String bookingKey = String.format(BOOKING_SEATS_KEY, bookingId);

        List<String> seats = redisTemplate.execute(allocateScript,
                Arrays.asList(showKey, bookingKey),
                seatCount,
                BOOKING_TTL_SECONDS
        );

        if (seats == null || seats.isEmpty())
            throw new SeatUnavailableException(showId, seatCount);

        return seats;
    }

    @Override
    public void releaseSeats(Long showId, List<String> seatIds) {
        if (seatIds == null || seatIds.isEmpty())
            return;

        String showKey = String.format(SHOW_SEATS_KEY, showId);
        redisTemplate.opsForSet().add(showKey, seatIds.toArray());
    }
}
