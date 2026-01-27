package com.abhinav.moviebooking.config;

import com.abhinav.moviebooking.booking.domain.Booking;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Booking> bookingRedisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Booking> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

}
