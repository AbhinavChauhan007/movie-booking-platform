package com.abhinav.moviebooking.booking.facade;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.store.InMemoryBookingStore;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.springframework.stereotype.Component;

@Component
public class BookingFacade {

    private final StandardBookingWorkflow standardBookingWorkflow;
    private final InMemoryBookingStore bookingStore;

    public BookingFacade(StandardBookingWorkflow standardBookingWorkflow, InMemoryBookingStore inMemoryBookingStore) {
        this.standardBookingWorkflow = standardBookingWorkflow;
        this.bookingStore = inMemoryBookingStore;
    }

    /**
     * Initiates a booking: creates Booking domain, creates ExecutionContext, and executes workflow.
     */
    public Booking initiateBooking(Long bookingId, Long showId, int seatCount, SeatType seatType) {

        // 1. create booking domain object
        Booking booking = bookingStore.create(bookingId);

        // 2. Create request context (runtime data)
        BookingExecutionContext context = new BookingExecutionContext(showId, seatCount, seatType);

        // 3. attach context for future cancel / expiry
        booking.attachExecutionContext(context);

        // execute workflow
        standardBookingWorkflow.execute(booking, context);

        return booking;
    }

    /**
     * Cancel a booking
     */
    public Booking cancelBooking(long bookingId) {
        Booking booking = bookingStore.findById(bookingId);
        standardBookingWorkflow.cancelBooking(booking);
        return booking;
    }

    /**
     * Expire a booking
     */
    public Booking expireBooking(long bookingId) {
        Booking booking = bookingStore.findById(bookingId);
        standardBookingWorkflow.expireBooking(booking);
        return booking;
    }

    /**
     * Fetch booking status
     */
    public BookingStatus getStatus(Long bookingId) {
        return bookingStore.findById(bookingId).getBookingStatus();
    }


}
