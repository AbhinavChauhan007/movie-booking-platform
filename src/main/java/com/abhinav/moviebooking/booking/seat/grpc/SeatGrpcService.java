package com.abhinav.moviebooking.booking.seat.grpc;

import com.abhinav.moviebooking.booking.exception.SeatUnavailableException;
import com.abhinav.moviebooking.booking.seat.core.SeatService;
import com.abhinav.moviebooking.grpc.seat.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@GrpcService
public class SeatGrpcService extends SeatServiceGrpc.SeatServiceImplBase {

    private final SeatService seatService;
    private final RedisTemplate<String, Object> redisTemplate;

    public SeatGrpcService(SeatService seatService, RedisTemplate<String, Object> redisTemplate) {
        this.seatService = seatService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void allocateSeats(AllocateSeatsRequest request, StreamObserver<AllocateSeatsResponse> responseObserver) {

        String idemKey = "idempotency:" + request.getIdempotencyKey();

        // check if the request has already been processed
        AllocateSeatsResponse cached = (AllocateSeatsResponse) redisTemplate.opsForValue().get(idemKey);

        if (cached != null) {
            responseObserver.onNext(cached);
            responseObserver.onCompleted();
            return;
        }

        try {
            List<String> seats = seatService.allocateSeats(
                    request.getShowId(),
                    request.getSeatCount(),
                    request.getBookingId()
            );

            AllocateSeatsResponse response = AllocateSeatsResponse.newBuilder()
                    .addAllSeatNumbers(seats)
                    .build();

            // store response in cached
            redisTemplate.opsForValue().set(idemKey, response, 10, TimeUnit.MINUTES);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SeatUnavailableException ex) {
            responseObserver.onError(
                    io.grpc.Status.FAILED_PRECONDITION
                            .withDescription(ex.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void releaseSeats(ReleaseSeatsRequest request, StreamObserver<ReleaseSeatsResponse> responseObserver) {
        String idemKey = "idempotency:" + request.getIdempotencyKey();

        // Check if already released
        Boolean alreadyReleased = redisTemplate.hasKey(idemKey);
        if (alreadyReleased) {
            responseObserver.onNext(
                    ReleaseSeatsResponse.newBuilder().setSuccess(true).build()
            );
            responseObserver.onCompleted();
            return;
        }
        try {
            seatService.releaseSeats(
                    request.getShowId(),
                    request.getSeatNumbersList()
            );

            // mark as released (no need to store payload)
            redisTemplate.opsForValue().set(idemKey, true, 10, TimeUnit.MINUTES);

            responseObserver.onNext(
                    ReleaseSeatsResponse.newBuilder().setSuccess(true).build()
            );
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to release seats")
                            .withCause(ex)
                            .asRuntimeException()
            );
        }
    }
}
