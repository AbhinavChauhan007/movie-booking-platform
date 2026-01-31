package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.payment.PaymentConfirmationService;
import com.abhinav.moviebooking.booking.payment.PaymentInitiationService;
import com.abhinav.moviebooking.booking.payment.PaymentResult;
import com.abhinav.moviebooking.booking.pricing.client.PricingGrpcClient;
import com.abhinav.moviebooking.booking.seat.client.SeatGrpcClient;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.show.entity.Show;
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

    private BookingIdempotencyGuard idempotencyGuard;
    private BookingCancellationService cancellationService;
    private PaymentInitiationService paymentInitiationService;
    private PaymentConfirmationService paymentConfirmationService;
    private SeatGrpcClient seatGrpcClient;
    private PricingGrpcClient pricingGrpcClient;
    private ShowRepository showRepository;
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

        workflow = new StandardBookingWorkflow(
                idempotencyGuard,
                cancellationService,
                paymentConfirmationService,
                seatGrpcClient,
                pricingGrpcClient,
                paymentInitiationService,
                showRepository
        );
    }

    @Test
    void testExecute_HappyPath() {
        Booking booking = Booking.newBooking();
        booking.assignId(1L); // ✅ REQUIRED

        long showId = 1L;
        int seatCount = 2;

        BookingExecutionContext context =
                new BookingExecutionContext(showId, seatCount, null);

        Show show = new Show(1L, Instant.now().plusSeconds(3600), 1, 100);
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));

        when(seatGrpcClient.allocateSeats(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of("A1", "A2"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(500.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.success("TXN-123"));

        workflow.execute(booking, context);

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        assertEquals(List.of("A1", "A2"), context.getAllocatedSeats());
        assertEquals(500.0, context.getFinalPrice());

        verify(seatGrpcClient)
                .allocateSeats(1L, showId, seatCount);

        verify(paymentInitiationService)
                .initiatePayment(eq(booking), eq(500.0), eq("1:PAYMENT"));
    }

    @Test
    void testExecute_ValidationFails_PastShow() {
        Booking booking = Booking.newBooking();
        booking.assignId(2L); // ✅ REQUIRED

        BookingExecutionContext context =
                new BookingExecutionContext(1L, 2, null);

        Show pastShow =
                new Show(1L, Instant.now().minusSeconds(3600), 1, 100);

        when(showRepository.findById(1L))
                .thenReturn(Optional.of(pastShow));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class,
                        () -> workflow.execute(booking, context));

        assertEquals("Cannot book past shows", ex.getMessage());
    }

    @Test
    void testExecute_PaymentFails_CompensatesBooking() {
        Booking booking = Booking.newBooking();
        booking.assignId(3L); // ✅ REQUIRED

        BookingExecutionContext context =
                new BookingExecutionContext(1L, 1, null);

        Show show =
                new Show(1L, Instant.now().plusSeconds(3600), 1, 50);

        when(showRepository.findById(1L))
                .thenReturn(Optional.of(show));

        when(seatGrpcClient.allocateSeats(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of("B1"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(200.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.failed());

        assertThrows(IllegalStateException.class,
                () -> workflow.execute(booking, context));

        verify(cancellationService)
                .cancelBooking(eq(3L), any());
    }

    @Test
    void testCancelBooking() {
        Booking booking = Booking.newBooking();
        booking.transitionTo(BookingStatus.INITIATED);

        workflow.cancelBooking(booking);

        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
    }

    @Test
    void testExpireBooking() {
        Booking booking = Booking.newBooking();
        booking.transitionTo(BookingStatus.INITIATED);

        workflow.expireBooking(booking);

        assertEquals(BookingStatus.EXPIRED, booking.getBookingStatus());
    }
}
