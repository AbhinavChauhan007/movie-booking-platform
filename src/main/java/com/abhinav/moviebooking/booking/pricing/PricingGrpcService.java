package com.abhinav.moviebooking.booking.pricing;

import com.abhinav.moviebooking.grpc.pricing.PricingRequest;
import com.abhinav.moviebooking.grpc.pricing.PricingResponse;
import com.abhinav.moviebooking.grpc.pricing.PricingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.redis.core.RedisTemplate;

@GrpcService
public class PricingGrpcService extends PricingServiceGrpc.PricingServiceImplBase {

    private final RedisTemplate<String, Object> redisTemplate;

    public PricingGrpcService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void calculatePrice(PricingRequest request, StreamObserver<PricingResponse> responseObserver) {

        String idemKey = "idempotency:" + request.getIdempotencyKey();

        PricingResponse cached = (PricingResponse) redisTemplate.opsForValue().get(idemKey);

        if(cached != null) {
            responseObserver.onNext(cached);
            responseObserver.onCompleted();
            return;
        }

        double basePrice = request.getBasePrice();
        boolean isPremiumUser = request.getPremiumUser();
        boolean isWeekend = request.getWeekend();

        double finalPrice = basePrice;

        if (isWeekend) {
            finalPrice = finalPrice * 1.20;
        }

        if (isPremiumUser) {
            finalPrice = finalPrice * 0.90;
        }


        PricingResponse response = PricingResponse
                .newBuilder()
                .setFinalPrice(finalPrice)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
