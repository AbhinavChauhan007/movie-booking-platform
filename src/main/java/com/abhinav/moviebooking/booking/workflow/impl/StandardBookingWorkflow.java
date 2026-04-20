package com.abhinav.moviebooking.booking.workflow.impl;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.InvalidBookingStateException;
import com.abhinav.moviebooking.booking.exception.InvalidSeatCountException;
import com.abhinav.moviebooking.booking.exception.PaymentException;
import com.abhinav.moviebooking.booking.payment.PaymentConfirmationService;
import com.abhinav.moviebooking.booking.payment.PaymentInitiationService;
import com.abhinav.moviebooking.booking.payment.PaymentResult;
import com.abhinav.moviebooking.booking.pricing.client.PricingGrpcClient;
import com.abhinav.moviebooking.booking.seat.client.SeatGrpcClient;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.BookingWorkflow;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.event.BookingCancelledEvent;
import com.abhinav.moviebooking.event.BookingConfirmedEvent;
import com.abhinav.moviebooking.event.service.EventPublisher;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.exception.MovieNotFoundException;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.exception.ShowNotFoundException;
import com.abhinav.moviebooking.show.exception.ShowValidationException;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import com.abhinav.moviebooking.util.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class StandardBookingWorkflow extends BookingWorkflow {

    private static final Logger log = LoggerFactory.getLogger(StandardBookingWorkflow.class);
    private static final int MAX_PAYMENT_RETRIES = 2;

    private final BookingIdempotencyGuard bookingIdempotencyGuard;
    private final BookingCancellationService bookingCancellationService;
    private final PaymentConfirmationService paymentConfirmationService;
    private final SeatGrpcClient seatGrpcClient;
    private final PricingGrpcClient pricingGrpcClient;
    private final PaymentInitiationService paymentInitiationService;
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final EventPublisher eventPublisher;

    public StandardBookingWorkflow(
            BookingIdempotencyGuard bookingIdempotencyGuard,
            BookingCancellationService bookingCancellationService,
            PaymentConfirmationService paymentConfirmationService,
            SeatGrpcClient seatGrpcClient,
            PricingGrpcClient pricingGrpcClient,
            PaymentInitiationService paymentInitiationService, ShowRepository showRepository, MovieRepository movieRepository, EventPublisher eventPublisher) {
        this.bookingIdempotencyGuard = bookingIdempotencyGuard;
        this.bookingCancellationService = bookingCancellationService;
        this.paymentConfirmationService = paymentConfirmationService;
        this.seatGrpcClient = seatGrpcClient;
        this.pricingGrpcClient = pricingGrpcClient;
        this.paymentInitiationService = paymentInitiationService;
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.eventPublisher = eventPublisher;
    }

    // ==================================================
    // Workflow Steps (Template Method implementation)
    // ==================================================
    // ================= VALIDATION =================

    @Override
    protected void validate(Booking booking, BookingExecutionContext context) throws InvalidBookingStateException, InvalidSeatCountException, ShowValidationException {
        log.debug("inside validate method");
        bookingIdempotencyGuard.checkExecutable(booking);

        if (context.getSeatCount() <= 0)
            throw new InvalidSeatCountException("Seat(s) must be more than zero");

        if (booking.getBookingStatus().isFinal())
            throw new InvalidBookingStateException("Booking is already in final state");

        Show show = showRepository.findByIdAndActiveTrue(context.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found"));

        // ✅ Fetch Movie using movieId from Show
        Movie movie = movieRepository.findByIdAndActiveTrue(show.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(show.getMovieId()));

        // ✅ Set movie title in context for Kafka event
        context.setMovieTitle(movie.getTitle());

        if (show.getStartTime().isBefore(Instant.now())) {
            throw new ShowValidationException(
                    ErrorCode.PAST_SHOW_BOOKING,
                    "Cannot book show " + show.getId() +
                            ". Show time has already passed: " + show.getStartTime()
            );
        }

        log.debug("Validating booking request for bookingId: {}", booking.getBookingId());
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

        log.info("Booking {} allocated seats {}", booking.getBookingId(), allocatedSeats);
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
        log.info("Final price: {}", finalPrice);
    }

    // ================= PAYMENT =================

    @Override
    protected void initiatePayment(Booking booking, BookingExecutionContext context) {

        String idempotencyKey = booking.getBookingId() + ":PAYMENT";
        int attempt = 0;

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                throw new PaymentException(ErrorCode.PAYMENT_INTERRUPTED, "Thread interrupted during payment");
            }

            PaymentResult result = paymentInitiationService.initiatePayment(
                    booking,
                    context.getFinalPrice(),
                    idempotencyKey
            );

            switch (result.getStatus()) {

                case SUCCESS -> {
                    log.info("Payment SUCCESS, txnId: {}", result.getTransactionId());
                    return;
                }

                case FAILED -> throw new PaymentException(ErrorCode.PAYMENT_FAILED, "Payment failed");

                case TIMEOUT -> {
                    attempt++;
                    if (attempt > MAX_PAYMENT_RETRIES) {
                        throw new PaymentException(ErrorCode.PAYMENT_TIMEOUT, "Payment TIMEOUT after retries");
                    }

                    log.warn("Payment TIMEOUT, retry attempt: {}", attempt);
                    sleepSafely(100L * attempt);
                }
            }
        }
    }

    // ================= CONFIRM =================

    @Override
    protected void confirmBooking(Booking booking, BookingExecutionContext context) {
        paymentConfirmationService.confirmPayment(booking);
        // Publish booking confirmed event
        BookingConfirmedEvent event = new BookingConfirmedEvent(
                booking.getBookingId(),
                booking.getUserId(),
                context.getShowId(),
                context.getMovieTitle(),
                context.getAllocatedSeats(),
                context.getFinalPrice()
        );
        eventPublisher.publishEvent(event);
        log.info("Booking {} CONFIRMED", booking.getBookingId());
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

        // Publish booking cancelled event
        BookingCancelledEvent event = new BookingCancelledEvent(
                booking.getBookingId(),
                booking.getUserId(),
                "PAYMENT_FAILED" // or appropriate reason
        );
        eventPublisher.publishEvent(event);
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

        log.info("Released seats {} for booking {}", context.getAllocatedSeats(), booking.getBookingId());
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
            throw new PaymentException(ErrorCode.PAYMENT_INTERRUPTED, "Payment retry interrupted", e);
        }
    }
}
