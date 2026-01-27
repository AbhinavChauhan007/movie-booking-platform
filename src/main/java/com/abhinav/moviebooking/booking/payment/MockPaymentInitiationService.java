package com.abhinav.moviebooking.booking.payment;

import com.abhinav.moviebooking.booking.domain.Booking;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MockPaymentInitiationService implements PaymentInitiationService {

    private final PaymentIdempotencyStore idempotencyStore;

    public MockPaymentInitiationService(
            PaymentIdempotencyStore idempotencyStore
    ) {
        this.idempotencyStore = idempotencyStore;
    }

    private PaymentResult processAndStore(String idempotencyKey) {

        int outcome = ThreadLocalRandom.current().nextInt(100);

        PaymentResult result;

        if (outcome < 70) {
            result = PaymentResult.success("TXN-" + UUID.randomUUID());
        } else if (outcome < 90) {
            result = PaymentResult.failed();
        } else {
            result = PaymentResult.timeout();
        }

        idempotencyStore.save(idempotencyKey, result);
        return result;
    }

    @Override
    public PaymentResult initiatePayment(Booking booking, double amount, String idempotencyKey) {
        return idempotencyStore.get(idempotencyKey)
                .orElseGet(
                        () -> processAndStore(idempotencyKey)
                );
    }
}
