package com.abhinav.moviebooking.booking.pricing.client;

import com.abhinav.moviebooking.booking.grpc.GrpcRetryExecutor;
import com.abhinav.moviebooking.grpc.pricing.PricingRequest;
import com.abhinav.moviebooking.grpc.pricing.PricingServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PricingGrpcClient {

    @GrpcClient("pricing-service")
    private PricingServiceGrpc.PricingServiceBlockingStub pricingServiceBlockingStub;

    public double calculatePrice(double basePrice, boolean weekend, boolean premiumUser) {
        PricingRequest request = PricingRequest.newBuilder()
                .setBasePrice(basePrice)
                .setPremiumUser(premiumUser)
                .setWeekend(weekend)
                .build();

        try {
            return GrpcRetryExecutor.executeWithRetry(
                    () -> pricingServiceBlockingStub
                            .withDeadlineAfter(80, TimeUnit.MILLISECONDS)
                            .calculatePrice(request)
                            .getFinalPrice(), 2
            );
        } catch (StatusRuntimeException ex) {
            throw PricingGrpcExceptionMapper.map(ex);
        }
    }

}
