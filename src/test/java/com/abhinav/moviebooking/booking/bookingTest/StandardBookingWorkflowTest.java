package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.PaymentException;
import com.abhinav.moviebooking.booking.payment.PaymentConfirmationService;
import com.abhinav.moviebooking.booking.payment.PaymentInitiationService;
import com.abhinav.moviebooking.booking.payment.PaymentResult;
import com.abhinav.moviebooking.booking.pricing.client.PricingGrpcClient;
import com.abhinav.moviebooking.booking.seat.client.SeatGrpcClient;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import com.abhinav.moviebooking.event.service.EventPublisher;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.exception.ShowValidationException;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StandardBookingWorkflowTest {

    private static final Long TEST_USER_ID = 789L;

    private BookingIdempotencyGuard idempotencyGuard;
    private BookingCancellationService cancellationService;
    private PaymentInitiationService paymentInitiationService;
    private PaymentConfirmationService paymentConfirmationService;
    private SeatGrpcClient seatGrpcClient;
    private PricingGrpcClient pricingGrpcClient;
    private ShowRepository showRepository;
    private MovieRepository movieRepository;
    private EventPublisher eventPublisher;
    private StandardBookingWorkflow workflow;


    @BeforeEach
    void setUp() {
        idempotencyGuard = mock(BookingIdempotencyGuard.class);
        cancellationService = mock(BookingCancellationService.class);
        paymentInitiationService = mock(PaymentInitiationService.class);
        paymentConfirmationService = mock(PaymentConfirmationService.class);
        seatGrpcClient = mock(SeatGrpcClient.class);
        pricingGrpcClient = mock(PricingGrpcClient.class);
        showRepository = mock(ShowRepository.class);
        movieRepository = mock(MovieRepository.class);
        eventPublisher = mock(EventPublisher.class);


        workflow = new StandardBookingWorkflow(
                idempotencyGuard,
                cancellationService,
                paymentConfirmationService,
                seatGrpcClient,
                pricingGrpcClient,
                paymentInitiationService,
                showRepository,
                movieRepository,
                eventPublisher
        );
    }

    @Test
    void execute_happyPath_confirmsBooking() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.assignId(1L);

        BookingExecutionContext context =
                new BookingExecutionContext(TEST_USER_ID, 10L, 2, null);

        // Create movie and show
        Movie movie = new Movie("Inception", "Sci-Fi", 148);
        movie.setId(1L);
        Show show = new Show(1L, Instant.now().plusSeconds(3600), 1, 100);

        when(showRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(show));
        when(movieRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(movie));

        when(seatGrpcClient.allocateSeats(1L, 10L, 2))
                .thenReturn(List.of("A1", "A2"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(500.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.success("TXN-1"));

        // Mock the confirmPayment to actually transition the booking to CONFIRMED
        doAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.transitionTo(BookingStatus.CONFIRMED);
            return null;
        }).when(paymentConfirmationService).confirmPayment(any(Booking.class));

        workflow.execute(booking, context);

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        assertEquals(List.of("A1", "A2"), context.getAllocatedSeats());
        assertEquals(500.0, context.getFinalPrice());

        verify(paymentConfirmationService).confirmPayment(booking);
    }

    @Test
    void execute_validationFails_pastShow() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.assignId(2L);

        BookingExecutionContext context =
                new BookingExecutionContext(TEST_USER_ID, 1L, 2, null);

        // Create movie and past show
        Movie movie = new Movie("Inception", "Sci-Fi", 148);
        movie.setId(1L);
        Show pastShow = new Show(1L, Instant.now().minusSeconds(3600), 1, 100);

        when(showRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(pastShow));
        when(movieRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(movie));

        assertThrows(ShowValidationException.class,
                () -> workflow.execute(booking, context));
    }

    @Test
    void execute_paymentFails_triggersCompensation() {
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.assignId(3L);

        BookingExecutionContext context =
                new BookingExecutionContext(TEST_USER_ID, 1L, 1, null);

        // Attach context to booking so compensation can access it
        booking.attachExecutionContext(context);

        // Create movie and show
        Movie movie = new Movie("Inception", "Sci-Fi", 148);
        movie.setId(1L);
        Show show = new Show(1L, Instant.now().plusSeconds(3600), 1, 50);

        when(showRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(show));
        when(movieRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(movie));

        when(seatGrpcClient.allocateSeats(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of("B1"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(200.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.failed());

        assertThrows(PaymentException.class,
                () -> workflow.execute(booking, context));

        // ✅ Seat released (showId is first parameter, bookingId is second)
        verify(seatGrpcClient)
                .releaseSeats(eq(1L), eq(3L), eq(List.of("B1")));

        // ✅ Cancellation delegated correctly
        verify(cancellationService)
                .cancelBooking(eq(3L), eq(BookingCancellationReason.SYSTEM_ERROR));
    }
}
