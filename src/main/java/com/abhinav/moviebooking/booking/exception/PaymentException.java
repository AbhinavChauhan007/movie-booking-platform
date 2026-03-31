package com.abhinav.moviebooking.booking.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class PaymentException extends BusinessException {
    public PaymentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PaymentException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message);
        initCause(cause);
    }
}
