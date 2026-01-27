package com.abhinav.moviebooking.booking.seat.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class SeatGrpcExceptionMapper {

    public static RuntimeException map(StatusRuntimeException ex) {

        Status.Code code = ex.getStatus().getCode();

        return switch (code) {
            case DEADLINE_EXCEEDED ->
                    new RuntimeException("Seat service timeout");
            case UNAVAILABLE ->
                    new RuntimeException("Seat service unavailable");
            case FAILED_PRECONDITION ->
                    new RuntimeException("Seats unavailable");
            default ->
                    new RuntimeException("Seat service error: " + code);
        };
    }
}
