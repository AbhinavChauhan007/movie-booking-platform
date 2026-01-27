package com.abhinav.moviebooking.booking.pricing.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Maps gRPC pricing service errors to domain-level runtime exceptions.
 * Keeps gRPC concerns outside the booking workflow.
 */
public final class PricingGrpcExceptionMapper {

    private PricingGrpcExceptionMapper() {
        // utility class
    }

    public static RuntimeException map(StatusRuntimeException ex) {

        Status.Code code = ex.getStatus().getCode();

        return switch (code) {

            case DEADLINE_EXCEEDED ->
                    new RuntimeException("Pricing service timeout");

            case UNAVAILABLE ->
                    new RuntimeException("Pricing service unavailable");

            case INVALID_ARGUMENT ->
                    new RuntimeException("Invalid pricing request");

            default ->
                    new RuntimeException(
                            "Pricing service error: " + code + " - " + ex.getStatus().getDescription()
                    );
        };
    }
}
