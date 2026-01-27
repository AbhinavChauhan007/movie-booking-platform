package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationService;
import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.payment.PaymentConfirmationService;
import com.abhinav.moviebooking.booking.payment.PaymentInitiationService;
import com.abhinav.moviebooking.booking.payment.PaymentResult;
import com.abhinav.moviebooking.booking.pricing.client.PricingGrpcClient;
import com.abhinav.moviebooking.booking.seat.client.SeatGrpcClient;
import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import com.abhinav.moviebooking.booking.workflow.BookingExecutionContext;
import com.abhinav.moviebooking.booking.workflow.guard.BookingIdempotencyGuard;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandardBookingWorkflowTest {

    @Mock
    BookingIdempotencyGuard bookingIdempotencyGuard;

    @Mock
    BookingCancellationService bookingCancellationService;

    @Mock
    PaymentConfirmationService paymentConfirmationService;

    @Mock
    SeatGrpcClient seatGrpcClient;

    @Mock
    PricingGrpcClient pricingGrpcClient;

    @Mock
    PaymentInitiationService paymentInitiationService;

    @InjectMocks
    StandardBookingWorkflow workflow;

    Booking booking;
    BookingExecutionContext context;

    @BeforeEach
    void setup() {
        booking = Booking.newBooking();
        booking.assignId(1L);

        context = new BookingExecutionContext(
                100L,
                2,
                SeatType.FIRST_AVAILABLE
        );

        booking.attachExecutionContext(context);
    }

    // ============================
    // ✅ Happy path
    // ============================
    @Test
    void shouldCompleteBookingSuccessfully() {

        when(seatGrpcClient.allocateSeats(1L, 100L, 2))
                .thenReturn(List.of("A1", "A2"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(400.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.success("txn-1"));

        workflow.execute(booking, context);

        verify(paymentConfirmationService).confirmPayment(1L);
        verify(bookingCancellationService, never()).cancelBooking(anyLong(), any());
        verify(seatGrpcClient, never()).releaseSeats(anyLong(), any());
    }

    // ============================
    // ⏱ TIMEOUT → retry → SUCCESS
    // ============================
    @Test
    void shouldRetryPaymentOnTimeoutAndSucceed() {

        when(seatGrpcClient.allocateSeats(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of("A1", "A2"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(400.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.timeout())
                .thenReturn(PaymentResult.success("txn-2"));

        workflow.execute(booking, context);

        verify(paymentInitiationService, times(2))
                .initiatePayment(any(), anyDouble(), anyString());

        verify(paymentConfirmationService).confirmPayment(1L);
    }

    // ============================
    // ❌ TIMEOUT beyond retries
    // ============================
    @Test
    void shouldCancelBookingAfterPaymentTimeoutRetries() {

        when(seatGrpcClient.allocateSeats(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of("A1", "A2"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(400.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.timeout())
                .thenReturn(PaymentResult.timeout())
                .thenReturn(PaymentResult.timeout());

        assertThrows(IllegalStateException.class,
                () -> workflow.execute(booking, context));

        verify(bookingCancellationService)
                .cancelBooking(eq(1L), any());

        verify(seatGrpcClient)
                .releaseSeats(100L, List.of("A1", "A2"));
    }

    // ============================
    // ❌ FAILED → no retry
    // ============================
    @Test
    void shouldFailImmediatelyOnPaymentFailure() {

        when(seatGrpcClient.allocateSeats(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of("A1", "A2"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(400.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.failed());

        assertThrows(IllegalStateException.class,
                () -> workflow.execute(booking, context));

        verify(paymentInitiationService, times(1))
                .initiatePayment(any(), anyDouble(), anyString());
    }

    // ============================
    // 🔍 Order verification (interview-grade)
    // ============================
    @Test
    void shouldExecuteWorkflowStepsInOrder() {

        when(seatGrpcClient.allocateSeats(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of("A1", "A2"));

        when(pricingGrpcClient.calculatePrice(anyDouble(), anyBoolean(), anyBoolean()))
                .thenReturn(400.0);

        when(paymentInitiationService.initiatePayment(any(), anyDouble(), anyString()))
                .thenReturn(PaymentResult.success("txn-3"));

        workflow.execute(booking, context);

        InOrder inOrder = inOrder(
                bookingIdempotencyGuard,
                seatGrpcClient,
                pricingGrpcClient,
                paymentInitiationService,
                paymentConfirmationService
        );

        inOrder.verify(bookingIdempotencyGuard).checkExecutable(booking);
        inOrder.verify(seatGrpcClient).allocateSeats(anyLong(), anyLong(), anyInt());
        inOrder.verify(pricingGrpcClient).calculatePrice(anyDouble(), anyBoolean(), anyBoolean());
        inOrder.verify(paymentInitiationService).initiatePayment(any(), anyDouble(), anyString());
        inOrder.verify(paymentConfirmationService).confirmPayment(1L);
    }
}
