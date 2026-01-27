package com.abhinav.moviebooking.booking.seat.client;

import com.abhinav.moviebooking.booking.grpc.GrpcRetryExecutor;
import com.abhinav.moviebooking.grpc.seat.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SeatGrpcClient {

    @GrpcClient("seat-service")
    private SeatServiceGrpc.SeatServiceBlockingStub seatServiceBlockingStub;

    /**
     * allocate seats after payment success
     * seat:{bookingId}:{operation} -> seat:12345:allocate
     */
    public List<String> allocateSeats(long bookingId, long showId, int seatCount) {
        AllocateSeatsRequest request = AllocateSeatsRequest.newBuilder()
                .setBookingId(bookingId)
                .setShowId(showId)
                .setSeatCount(seatCount)
                .setIdempotencyKey("seat:" + bookingId + ":allocate")
                .build();

        try {
            return GrpcRetryExecutor.executeWithRetry(
                    () ->
                            seatServiceBlockingStub
                                    .withDeadlineAfter(250, TimeUnit.MILLISECONDS)
                                    .allocateSeats(request)
                                    .getSeatNumbersList(), 2
            );
        } catch (StatusRuntimeException e) {
            throw SeatGrpcExceptionMapper.map(e);
        }
    }

    /**
     * Release seats on failure / timeout
     * * seat:{bookingId}:{operation} -> seat:12345:release
     */
    public void releaseSeats(long showId, List<String> seats) {
        ReleaseSeatsRequest request = ReleaseSeatsRequest.newBuilder()
                .setShowId(showId)
                .addAllSeatNumbers(seats)
                .setIdempotencyKey("seat:" + showId + ":release")
                .build();

        try {
            seatServiceBlockingStub
                    .withDeadlineAfter(100, TimeUnit.MILLISECONDS)
                    .releaseSeats(request);
        } catch (StatusRuntimeException e) {
            throw SeatGrpcExceptionMapper.map(e);
        }
    }

}
