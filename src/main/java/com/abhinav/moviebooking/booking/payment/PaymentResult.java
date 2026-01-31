package com.abhinav.moviebooking.booking.payment;

import java.io.Serializable;

public class PaymentResult implements Serializable {

    private final PaymentStatus status;
    private final String transactionId;

    public PaymentResult(PaymentStatus status, String transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }

    public static PaymentResult success(String txnId){
        return new PaymentResult(PaymentStatus.SUCCESS,txnId);
    }

    public static PaymentResult failed() {
        return new PaymentResult(PaymentStatus.FAILED, null);
    }

    public static PaymentResult timeout() {
        return new PaymentResult(PaymentStatus.TIMEOUT, null);
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
