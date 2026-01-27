package com.abhinav.moviebooking.booking.facade;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.entity.BookingIdempotencyEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingIdempotencyRepository;
import com.abhinav.moviebooking.booking.read.BookingReadService;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class BookingFacade {

    private final StandardBookingWorkflow standardBookingWorkflow;
    private final BookingPersistenceAdapter bookingPersistenceAdapter;
    private final BookingReadService bookingReadService;
    private final BookingCache bookingCache;
    private final BookingIdempotencyRepository bookingIdempotencyRepository;

    public BookingFacade(StandardBookingWorkflow standardBookingWorkflow,
                         BookingPersistenceAdapter bookingPersistenceAdapter,
                         BookingReadService bookingReadService,
                         BookingCache bookingCache,
                         BookingIdempotencyRepository bookingIdempotencyRepository) {
        this.standardBookingWorkflow = standardBookingWorkflow;
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.bookingReadService = bookingReadService;
        this.bookingCache = bookingCache;
        this.bookingIdempotencyRepository = bookingIdempotencyRepository;
    }

    /**
     * Initiates a booking in a safe, idempotent manner.
     */
    @Transactional
    public Booking initiateBooking(Long showId, int seatCount, SeatType seatType, String idempotencyKey) {

        // --------------------------------------------------
        // 1. Fast idempotency read path
        // --------------------------------------------------
        BookingIdempotencyEntity existing =
                bookingIdempotencyRepository.findById(idempotencyKey).orElse(null);

        if (existing != null) {
            return bookingReadService.getBooking(existing.getBookingId());
        }

        // --------------------------------------------------
        // 2. Create & persist booking EARLY (ID required)
        // --------------------------------------------------
        Booking booking = Booking.newBooking();
        Booking persistedBooking = bookingPersistenceAdapter.save(booking);

        // --------------------------------------------------
        // 3. Register idempotency BEFORE workflow execution
        // --------------------------------------------------
        try {
            bookingIdempotencyRepository.save(
                    new BookingIdempotencyEntity(
                            idempotencyKey,
                            persistedBooking.getBookingId()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            // Another request won the race → return existing booking
            return bookingReadService.getBooking(
                    bookingIdempotencyRepository.findById(idempotencyKey)
                            .map(BookingIdempotencyEntity::getBookingId)
                            .orElseThrow()
            );
        }

        // --------------------------------------------------
        // 4. Attach runtime execution context
        // --------------------------------------------------
        BookingExecutionContext context =
                new BookingExecutionContext(showId, seatCount, seatType);

        persistedBooking.attachExecutionContext(context);

        // --------------------------------------------------
        // 5. Execute workflow (seat → price → payment → confirm)
        // --------------------------------------------------
        standardBookingWorkflow.execute(persistedBooking, context);

        // --------------------------------------------------
        // 6. Persist final state & cache
        // --------------------------------------------------
        Booking finalBooking = bookingPersistenceAdapter.save(persistedBooking);
        bookingCache.put(finalBooking);

        return finalBooking;
    }

    /**
     * Cancel booking
     */
    @Transactional
    public Booking cancelBooking(long bookingId) {
        Booking booking = bookingReadService.getBooking(bookingId);

        standardBookingWorkflow.cancelBooking(booking);

        Booking savedBooking = bookingPersistenceAdapter.save(booking);
        bookingCache.put(savedBooking);

        return savedBooking;
    }

    /**
     * Expire booking
     */
    @Transactional
    public Booking expireBooking(long bookingId) {
        Booking booking = bookingReadService.getBooking(bookingId);

        standardBookingWorkflow.expireBooking(booking);

        Booking savedBooking = bookingPersistenceAdapter.save(booking);
        bookingCache.put(savedBooking);

        return savedBooking;
    }

    /**
     * Fetch booking status
     */
    public BookingStatus getStatus(Long bookingId) {
        return bookingReadService.getBooking(bookingId).getBookingStatus();
    }
}
