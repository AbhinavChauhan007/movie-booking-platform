package com.abhinav.moviebooking.booking.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.Set;
import java.util.function.Supplier;

public class GrpcRetryExecutor {

    private static final Set<Status.Code> RETRYABLE = Set.of(Status.Code.DEADLINE_EXCEEDED, Status.Code.UNAVAILABLE);

    public static <T> T executeWithRetry(Supplier<T> action, int maxAttempts) {
        int attempt = 0;
        while (true) {
            try {
                return action.get();
            } catch (StatusRuntimeException e) {
                attempt++;

                if (attempt >= maxAttempts || !RETRYABLE.contains(e.getStatus().getCode())) {
                    throw e;
                }

                try {
                    Thread.sleep(20L * attempt); // exponential-ish backoff
                } catch (InterruptedException ignored) {
                }
            }
        }
    }


}
