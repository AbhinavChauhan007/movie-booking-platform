package com.abhinav.moviebooking.booking.seat.grpc;

import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.exception.SeatUnavailableException;
import com.abhinav.moviebooking.booking.seat.core.impl.RedisSeatService;
import com.abhinav.moviebooking.grpc.seat.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@GrpcService
public class SeatGrpcService extends SeatServiceGrpc.SeatServiceImplBase {

    private final RedisSeatService redisSeatService;
    private final RedisTemplate<String, Object> redisTemplate;

    public SeatGrpcService(RedisSeatService redisSeatService, RedisTemplate<String, Object> redisTemplate) {
        this.redisSeatService = redisSeatService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void allocateSeats(AllocateSeatsRequest request,
                              StreamObserver<AllocateSeatsResponse> responseObserver) {

        String idemKey = "idempotency:" + request.getIdempotencyKey();

        try {
            // ---- idempotency check
            @SuppressWarnings("unchecked")
            List<String> cachedSeats =
                    (List<String>) redisTemplate.opsForValue().get(idemKey);

            if (cachedSeats != null) {
                responseObserver.onNext(
                        AllocateSeatsResponse.newBuilder()
                                .addAllSeatNumbers(cachedSeats)
                                .build()
                );
                responseObserver.onCompleted();
                return;
            }

            // ---- domain call
            List<String> allocatedSeats = redisSeatService.allocateSeats(
                    request.getShowId(),
                    request.getSeatCount(),
                    request.getBookingId()
            );

            // ---- cache domain data only
            redisTemplate.opsForValue().set(
                    idemKey,
                    allocatedSeats,
                    10,
                    TimeUnit.MINUTES
            );

            responseObserver.onNext(
                    AllocateSeatsResponse.newBuilder()
                            .addAllSeatNumbers(allocatedSeats)
                            .build()
            );
            responseObserver.onCompleted();

        } catch (SeatUnavailableException ex) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (BookingNotFoundException ex) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            ex.printStackTrace(); // TEMP: remove later
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Seat service crashed").asRuntimeException()
            );
        }
    }


    @Override
    public void releaseSeats(ReleaseSeatsRequest request, StreamObserver<ReleaseSeatsResponse> responseObserver) {
        String idemKey = "idempotency:" + request.getIdempotencyKey();

        // -----------------------------
        // Check if already released
        // -----------------------------
        Boolean alreadyReleased = redisTemplate.hasKey(idemKey);
        if (alreadyReleased != null && alreadyReleased) {
            responseObserver.onNext(
                    ReleaseSeatsResponse.newBuilder().setSuccess(true).build()
            );
            responseObserver.onCompleted();
            return;
        }

        try {
            // -----------------------------
            // Release seats via SeatBookingService
            // -----------------------------
            redisSeatService.releaseSeats(
                    request.getBookingId(), // Optional: can also release by showId + seatNumbers if needed
                    request.getSeatNumbersList()
            );

            // -----------------------------
            // Mark as released for idempotency
            // -----------------------------
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
