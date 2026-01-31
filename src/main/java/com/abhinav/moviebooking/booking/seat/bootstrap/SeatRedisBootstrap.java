package com.abhinav.moviebooking.booking.seat.bootstrap;

import com.abhinav.moviebooking.booking.seat.core.SeatService;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SeatRedisBootstrap {

    private static final String SHOW_SEATS_KEY = "show:%d:available_seats";

    private final ShowRepository showRepository;
    private final SeatService seatService;
    private final StringRedisTemplate redisTemplate;

    public SeatRedisBootstrap(
            ShowRepository showRepository,
            SeatService seatService,
            StringRedisTemplate redisTemplate

            ) {
        this.showRepository = showRepository;
        this.seatService = seatService;
        this.redisTemplate = redisTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rehydrateSeats() {
        System.out.println(("Rehydrating seats for future shows"));
        showRepository.findAllFutureShows(Instant.now())
                .forEach(show -> {
                    String redisKey =
                            String.format(SHOW_SEATS_KEY, show.getId());

                    if (!redisTemplate.hasKey(redisKey)) {
                        seatService.initializeSeats(
                                show.getId(),
                                show.getTotalSeats()
                        );
                    }
                });
    }
}
