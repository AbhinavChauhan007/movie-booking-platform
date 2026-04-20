package com.abhinav.moviebooking.booking.seat.bootstrap;

import com.abhinav.moviebooking.booking.seat.core.SeatService;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SeatRedisBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SeatRedisBootstrap.class);
    private static final String SHOW_SEATS_KEY = "show:%d:available_seats";
    private static final int BATCH_SIZE = 100;

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
        log.info("Rehydrating seats for future shows");

        int pageNumber = 0, totalProcessed = 0;
        Page<Show> showPage;
        do {
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            showPage = showRepository.findFutureShows(Instant.now(), pageable);

            showPage.getContent().forEach(show -> {
                try {
                    String redisKey =
                            String.format(SHOW_SEATS_KEY, show.getId());

                    if (!redisTemplate.hasKey(redisKey)) {
                        seatService.initializeSeats(
                                show.getId(),
                                show.getTotalSeats()
                        );
                        log.debug("Initialized seats for show ID: {}", show.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to initialize seats for show ID: {}. Error: {}",
                            show.getId(), e.getMessage(), e);
                }
            });
            totalProcessed += showPage.getNumberOfElements();
            pageNumber++;

            log.info("Processed page {} of {} ({}/{} shows)",
                    pageNumber, showPage.getTotalPages(), totalProcessed, showPage.getTotalElements());

        } while (showPage.hasNext());

        log.info("Seat rehydration completed for {} shows", showPage.getTotalElements());

    }
}
