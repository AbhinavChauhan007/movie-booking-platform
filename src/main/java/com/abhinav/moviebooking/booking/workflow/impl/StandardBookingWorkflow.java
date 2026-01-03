package com.abhinav.moviebooking.booking.workflow.impl;

import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategyFactory;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.state.impl.ConfirmedState;
import com.abhinav.moviebooking.booking.state.impl.InitiatedState;
import com.abhinav.moviebooking.booking.workflow.BookingWorkflow;
import org.springframework.stereotype.Component;

@Component
public class StandardBookingWorkflow extends BookingWorkflow {

    // runtime data (set by facade)
    private Long bookingId;
    private Long showId;
    private int seatCount;
    private SeatType seatType;

    private final SeatAllocationStrategyFactory seatAllocationStrategyFactory;
    private final InitiatedState initiatedState;
    private final ConfirmedState confirmedState;

    public StandardBookingWorkflow(SeatAllocationStrategyFactory seatAllocationStrategyFactory, InitiatedState initiatedState, ConfirmedState confirmedState) {
        this.seatAllocationStrategyFactory = seatAllocationStrategyFactory;
        this.initiatedState = initiatedState;
        this.confirmedState = confirmedState;
    }

    public void init(SeatType seatType, int seatCount, Long showId, Long bookingId) {
        this.seatType = seatType;
        this.seatCount = seatCount;
        this.showId = showId;
        this.bookingId = bookingId;
    }

    @Override
    protected void validate() {
        // validate booking request
    }

    @Override
    protected void allocateSeats() {
        // delegate to SeatAllocationStrategy
        SeatAllocationStrategy seatAllocationStrategy = seatAllocationStrategyFactory.getStrategy(seatType);

        seatAllocationStrategy.allocateSeats(showId, seatCount);
    }

    @Override
    protected void calculatePrice() {
        // delegate to PricingStrategy
    }

    @Override
    protected void initiatePayment() {
        // payment gateway
    }

    @Override
    protected void confirmBooking() {
        // move booking to CONFIRMED state
        initiatedState.handle(bookingId);
        confirmedState.handle(bookingId);
    }

    public void confirmOnly(Long bookingId) {
        confirmedState.handle(bookingId);
    }

    public void allocateSeatsOnly(Long showId, int seatCount, SeatType seatType) {
        SeatAllocationStrategy seatAllocationStrategy = seatAllocationStrategyFactory.getStrategy(seatType);
        seatAllocationStrategy.allocateSeats(showId, seatCount);
    }
}

