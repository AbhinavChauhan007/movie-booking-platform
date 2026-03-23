package com.abhinav.moviebooking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.booking-events}")
    private String bookingEventsTopic;

    @Value("${kafka.topic.booking-events-dlq}")
    private String bookingEventsDlqTopic;

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name(bookingEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingEventsDlqTopic() {
        return TopicBuilder.name(bookingEventsDlqTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }


}
