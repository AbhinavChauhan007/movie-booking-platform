package com.abhinav.moviebooking.grpc;

import io.grpc.*;

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@GrpcGlobalServerInterceptor
public class GrpcIdempotencyInterceptor implements ServerInterceptor {

    private static final String IDEMPOTENCY_HEADER = "idempotency-key";
    private static final Metadata.Key<String> IDEMPOTENCY_KEY =
            Metadata.Key.of(IDEMPOTENCY_HEADER, Metadata.ASCII_STRING_MARSHALLER);

    private final RedisTemplate<String, Object> redisTemplate;

    public GrpcIdempotencyInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {

        String idemKey = headers.get(IDEMPOTENCY_KEY);

        if (idemKey == null || idemKey.isBlank()) {
            return next.startCall(call, headers);
        }

        String redisKey = "grpc:idempotency:" + idemKey;

        @SuppressWarnings("unchecked")
        RespT cachedResponse = (RespT) redisTemplate.opsForValue().get(redisKey);

        if (cachedResponse != null) {
            return replayCachedResponse(call, cachedResponse);
        }

        ServerCall<ReqT, RespT> wrappedCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

                    @Override
                    public void sendMessage(RespT message) {
                        redisTemplate.opsForValue()
                                .set(redisKey, message, 10, TimeUnit.MINUTES);
                        super.sendMessage(message);
                    }
                };

        return next.startCall(wrappedCall, headers);
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> replayCachedResponse(
            ServerCall<ReqT, RespT> call,
            RespT cachedResponse
    ) {

        call.sendHeaders(new Metadata());
        call.sendMessage(cachedResponse);
        call.close(Status.OK, new Metadata());

        return new ServerCall.Listener<ReqT>() {
            // no-op listener
        };
    }
}
