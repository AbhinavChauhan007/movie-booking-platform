package com.abhinav.moviebooking.booking.workflow.impl;

import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.strategy.SeatAllocationStrategyFactory;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.state.impl.ConfirmedState;
import com.abhinav.moviebooking.booking.state.impl.InitiatedState;
import com.abhinav.moviebooking.booking.workflow.BookingWorkflow;
import com.abhinav.moviebooking.pricing.context.PricingContext;
import com.abhinav.moviebooking.pricing.context.PricingRequest;
import com.abhinav.moviebooking.pricing.strategy.PricingStrategy;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class StandardBookingWorkflow extends BookingWorkflow {

    // runtime data (set by facade)
    private Long bookingId;
    private Long showId;
    private int seatCount;
    private SeatType seatType;

    // ===== Calculated values =====
    private double finalPrice;

    private final SeatAllocationStrategyFactory seatAllocationStrategyFactory;
    private final InitiatedState initiatedState;
    private final ConfirmedState confirmedState;
    private final PricingContext pricingContext;

    public StandardBookingWorkflow(SeatAllocationStrategyFactory seatAllocationStrategyFactory, InitiatedState initiatedState, ConfirmedState confirmedState, PricingContext pricingContext) {
        this.seatAllocationStrategyFactory = seatAllocationStrategyFactory;
        this.initiatedState = initiatedState;
        this.confirmedState = confirmedState;
        this.pricingContext = pricingContext;
    }

    /**
     * Initialize workflow with runtime data.
     * This method MUST be called before execute().
     */
    public void init(SeatType seatType, int seatCount, Long showId, Long bookingId) {
        this.seatType = seatType;
        this.seatCount = seatCount;
        this.showId = showId;
        this.bookingId = bookingId;
    }

    // ==================================================
    // Workflow Steps (Template Method implementation)
    // ==================================================

    @Override
    protected void validate() {
        // Placeholder for validation rules
        // Example: seatCount > 0, show exists, bookingId unique, etc.
        System.out.println("Validating booking request for bookingId=" + bookingId);
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
        double basePrice = seatCount * 200; // temporary pricing value

        PricingRequest pricingRequest = new PricingRequest(
                basePrice,
                isWeekend(),
                false // assume non - premium user for now
        );

        PricingStrategy pricingStrategy = pricingContext.resolve(pricingRequest);

        this.finalPrice = pricingStrategy.calculatePrice(pricingRequest);

        System.out.println("Final price for booking " + bookingId + " is " + finalPrice);
    }

    @Override
    protected void initiatePayment() {
        // payment gateway
        System.out.println(
                "Initiating payment for booking " + bookingId +
                        " with amount " + finalPrice
        );
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

    // ==================================================
    // Helper methods
    // ==================================================

    private boolean isWeekend() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
    }
}

