package com.abhinav.moviebooking.booking.payment;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisPaymentIdempotencyStore implements PaymentIdempotencyStore {

    private static final Duration TTL = Duration.ofMinutes(15);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPaymentIdempotencyStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<PaymentResult> get(String idempotencyKey) {
        return Optional.ofNullable(
                (PaymentResult) redisTemplate.opsForValue().get(key(idempotencyKey))
        );
    }

    @Override
    public void save(String idempotencyKey, PaymentResult result) {
        redisTemplate.opsForValue().set(key(idempotencyKey), result, TTL);
    }

    private String key(String idempotencyKey) {
        return "payment:idempotency:" + idempotencyKey;
    }

}
