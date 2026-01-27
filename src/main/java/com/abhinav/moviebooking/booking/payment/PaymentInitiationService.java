package com.abhinav.moviebooking.booking.payment;

import com.abhinav.moviebooking.booking.domain.Booking;

public interface PaymentInitiationService {

    PaymentResult initiatePayment(Booking booking, double amount , String idempotencyKey);
}
