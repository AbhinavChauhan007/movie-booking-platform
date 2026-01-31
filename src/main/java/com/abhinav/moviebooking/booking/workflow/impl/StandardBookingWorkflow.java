package com.abhinav.moviebooking.booking.workflow.impl;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.payment.PaymentConfirmationService;
import com.abhinav.moviebooking.booking.payment.PaymentInitiationService;
import com.abhinav.moviebooking.booking.payment.PaymentResult;
import com.abhinav.moviebooking.booking.pricing.client.PricingGrpcClient;
import com.abhinav.moviebooking.booking.seat.client.SeatGrpcClient;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.BookingWorkflow;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class StandardBookingWorkflow extends BookingWorkflow {

    private static final int MAX_PAYMENT_RETRIES = 2;

    private final BookingIdempotencyGuard bookingIdempotencyGuard;
    private final BookingCancellationService bookingCancellationService;
    private final PaymentConfirmationService paymentConfirmationService;
    private final SeatGrpcClient seatGrpcClient;
    private final PricingGrpcClient pricingGrpcClient;
    private final PaymentInitiationService paymentInitiationService;
    private final ShowRepository showRepository;

    public StandardBookingWorkflow(
            BookingIdempotencyGuard bookingIdempotencyGuard,
            BookingCancellationService bookingCancellationService,
            PaymentConfirmationService paymentConfirmationService,
            SeatGrpcClient seatGrpcClient,
            PricingGrpcClient pricingGrpcClient,
            PaymentInitiationService paymentInitiationService, ShowRepository showRepository) {
        this.bookingIdempotencyGuard = bookingIdempotencyGuard;
        this.bookingCancellationService = bookingCancellationService;
        this.paymentConfirmationService = paymentConfirmationService;
        this.seatGrpcClient = seatGrpcClient;
        this.pricingGrpcClient = pricingGrpcClient;
        this.paymentInitiationService = paymentInitiationService;
        this.showRepository = showRepository;
    }

    // ==================================================
    // Workflow Steps (Template Method implementation)
    // ==================================================
    // ================= VALIDATION =================

    @Override
    protected void validate(Booking booking, BookingExecutionContext context) {

        bookingIdempotencyGuard.checkExecutable(booking);

        if (context.getSeatCount() <= 0)
            throw new IllegalArgumentException("Seat(s) must be more than zero");

        if (booking.getBookingStatus().isFinal())
            throw new IllegalArgumentException("Booking is already in final state");

        Show show = showRepository.findById(context.getShowId())
                .orElseThrow(() -> new IllegalArgumentException("Show not found"));

        if (show.getStartTime().isBefore(Instant.now())) {
            throw new IllegalStateException("Cannot book past shows");
        }

        System.out.println("Validating booking request for bookingId : " + booking.getBookingId());
    }

    // ================= SEAT ALLOCATION =================

    @Override
    protected void allocateSeats(Booking booking, BookingExecutionContext context) {

        List<String> allocatedSeats = seatGrpcClient.allocateSeats(
                booking.getBookingId(),
                context.getShowId(),
                context.getSeatCount()
        );


        context.setAllocatedSeats(allocatedSeats);
        booking.transitionTo(BookingStatus.INITIATED);

        System.out.println(
                "Booking " + booking.getBookingId() +
                        " allocated seats " + allocatedSeats
        );
    }

    // ================= PRICING =================

    @Override
    protected void calculatePrice(Booking booking, BookingExecutionContext context) {

        double basePrice = context.getSeatCount() * 200;

        double finalPrice = pricingGrpcClient.calculatePrice(
                basePrice,
                isWeekend(),
                false
        );

        context.setFinalPrice(finalPrice);
        System.out.println("Final price = " + finalPrice);
    }

    // ================= PAYMENT =================

    @Override
    protected void initiatePayment(Booking booking, BookingExecutionContext context) {

        String idempotencyKey = booking.getBookingId() + ":PAYMENT";
        int attempt = 0;

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                throw new IllegalStateException("Thread interrupted during payment");
            }

            PaymentResult result = paymentInitiationService.initiatePayment(
                    booking,
                    context.getFinalPrice(),
                    idempotencyKey
            );

            switch (result.getStatus()) {

                case SUCCESS -> {
                    System.out.println("Payment SUCCESS, txnId=" + result.getTransactionId());
                    return;
                }

                case FAILED -> throw new IllegalStateException("Payment FAILED");

                case TIMEOUT -> {
                    attempt++;
                    if (attempt > MAX_PAYMENT_RETRIES) {
                        throw new IllegalStateException("Payment TIMEOUT after retries");
                    }

                    System.out.println("Payment TIMEOUT, retry " + attempt);
                    sleepSafely(100L * attempt);
                }
            }
        }
    }

    // ================= CONFIRM =================

    @Override
    protected void confirmBooking(Booking booking, BookingExecutionContext context) {
        paymentConfirmationService.confirmPayment(booking);

        System.out.println("Booking " + booking.getBookingId() + " CONFIRMED");
    }

    // ================= COMPENSATION =================

    @Override
    protected void compensate(Booking booking) {

        // 1️⃣ Release external resources FIRST
        releaseSeatsIfAllocated(booking);

        // 2️⃣ Then mark booking cancelled
        bookingCancellationService.cancelBooking(
                booking.getBookingId(),
                BookingCancellationReason.SYSTEM_ERROR
        );
    }

    @Override
    protected void releaseSeatsIfAllocated(Booking booking) {

        BookingExecutionContext context = booking.getBookingExecutionContext();

        if (context == null ||
                context.getAllocatedSeats() == null ||
                context.getAllocatedSeats().isEmpty()) {
            return;
        }

        seatGrpcClient.releaseSeats(
                context.getShowId(),
                booking.getBookingId(),
                context.getAllocatedSeats()
        );

        System.out.println(
                "Released seats " + context.getAllocatedSeats() +
                        " for booking " + booking.getBookingId()
        );
    }

    // ================= HELPERS =================

    private boolean isWeekend() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
    }

    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Payment retry interrupted", e);
        }
    }
}
