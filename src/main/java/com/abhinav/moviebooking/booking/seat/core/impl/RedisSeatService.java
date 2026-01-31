package com.abhinav.moviebooking.booking.seat.core.impl;

import com.abhinav.moviebooking.booking.exception.SeatUnavailableException;
import com.abhinav.moviebooking.booking.seat.core.SeatService;
import io.lettuce.core.RedisCommandExecutionException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RedisSeatService implements SeatService {

    private static final String SHOW_SEATS_KEY = "show:%d:available_seats";
    private static final String BOOKING_SEATS_KEY = "booking:%s:locked_seats";
    private static final int BOOKING_TTL_SECONDS = 600; // 10 minutes

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> allocateScript;

    public RedisSeatService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.allocateScript = RedisScript.of(
                new ClassPathResource("lua/allocate_seats.lua"),
                List.class
        );
    }

    @Override
    public void initializeSeats(Long showId, int totalSeats) {
        String showKey = String.format(SHOW_SEATS_KEY, showId);

        for (int i = 1; i <= totalSeats; i++) {
            redisTemplate.opsForSet().add(showKey, "A" + i);
        }
    }

    @Override
    public List<String> allocateSeats(Long showId, int seatCount, Long bookingId) {
        String showKey = String.format(SHOW_SEATS_KEY, showId);
        String bookingKey = String.format(BOOKING_SEATS_KEY, bookingId);

        try {
            @SuppressWarnings("unchecked")
            List<String> seats = redisTemplate.execute(
                    allocateScript,
                    Arrays.asList(showKey, bookingKey),
                    String.valueOf(seatCount),
                    String.valueOf(BOOKING_TTL_SECONDS)
            );

            if (seats == null || seats.isEmpty()) {
                throw new SeatUnavailableException(showId, seatCount);
            }

            return seats;

        } catch (RedisCommandExecutionException e) {
            String msg = e.getMessage();

            if (msg != null && (
                    msg.contains("INSUFFICIENT_SEATS") ||
                            msg.contains("SEATS_NOT_INITIALIZED") ||
                            msg.contains("INVALID_SEAT_COUNT") ||
                            msg.contains("INVALID_TTL")
            )) {
                throw new SeatUnavailableException(showId, seatCount);
            }

            throw e; // real Redis failure
        }
    }

    @Override
    public void releaseSeats(Long showId, List<String> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }

        String showKey = String.format(SHOW_SEATS_KEY, showId);
        redisTemplate.opsForSet().add(showKey, seatIds.toArray(new String[0]));
    }
}
