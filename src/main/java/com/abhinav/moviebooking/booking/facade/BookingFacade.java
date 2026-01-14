package com.abhinav.moviebooking.booking.facade;

import com.abhinav.moviebooking.booking.cache.BookingCache;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.entity.BookingIdempotencyEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingIdempotencyRepository;
import com.abhinav.moviebooking.booking.read.BookingReadService;
import com.abhinav.moviebooking.booking.seat.SeatType;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
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
                         BookingReadService bookingReadService, BookingCache bookingCache, BookingIdempotencyRepository bookingIdempotencyRepository) {
        this.standardBookingWorkflow = standardBookingWorkflow;
        this.bookingPersistenceAdapter = bookingPersistenceAdapter;
        this.bookingReadService = bookingReadService;
        this.bookingCache = bookingCache;
        this.bookingIdempotencyRepository = bookingIdempotencyRepository;
    }

    /**
     * Initiates a booking: creates Booking domain, creates ExecutionContext, and executes workflow.
     */
    public Booking initiateBooking(Long showId, int seatCount, SeatType seatType, String idempotencyKey) {

        // check if this idempotency key already exists
        BookingIdempotencyEntity existing = bookingIdempotencyRepository.
                findById(idempotencyKey)
                .orElse(null);
        if (existing != null) {
            return bookingReadService.getBooking(existing.getBookingId());
        }

        // Normal booking flow

        // 1. Create Booking object
        Booking booking = new Booking();

        // 2. Create request context (runtime data)
        BookingExecutionContext context = new BookingExecutionContext(showId, seatCount, seatType);

        // 3. attach context for future cancel / expiry
        booking.attachExecutionContext(context);

        // 4. execute workflow
        standardBookingWorkflow.execute(booking, context);

        // 5. write through
        Booking savedBooking = bookingPersistenceAdapter.save(booking);
        bookingCache.put(savedBooking);

        // save idempotency mapping
        try {
            bookingIdempotencyRepository.save(new BookingIdempotencyEntity(
                    idempotencyKey,
                    savedBooking.getBookingId()
            ));
        } catch (DataIntegrityViolationException e) {
            // Another request has already saved the key concurrently
            return bookingReadService.getBooking(
                    bookingIdempotencyRepository.findById(idempotencyKey)
                            .map(BookingIdempotencyEntity::getBookingId)
                            .orElseThrow()
            );
        }

        return savedBooking;
    }

    /**
     * Cancel a booking
     */
    public Booking cancelBooking(long bookingId) {
        Booking booking = bookingReadService.getBooking(bookingId);

        standardBookingWorkflow.cancelBooking(booking);

        Booking savedBooking = bookingPersistenceAdapter.save(booking);
        bookingCache.put(savedBooking);

        return booking;
    }

    /**
     * Expire a booking
     */
    public Booking expireBooking(long bookingId) {
        Booking booking = bookingReadService.getBooking(bookingId);

        standardBookingWorkflow.expireBooking(booking);

        Booking savedBooking = bookingPersistenceAdapter.save(booking);
        bookingCache.put(savedBooking);

        return booking;
    }

    /**
     * Fetch booking status
     */
    public BookingStatus getStatus(Long bookingId) {
        return bookingReadService.getBooking(bookingId).getBookingStatus();
    }


}
