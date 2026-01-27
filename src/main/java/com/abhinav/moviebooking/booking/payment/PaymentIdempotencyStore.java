package com.abhinav.moviebooking.booking.payment;

import java.util.Optional;

public interface PaymentIdempotencyStore {

    Optional<PaymentResult> get(String idempotencyKey);

    void save(String idempotencyKey, PaymentResult result);
}
