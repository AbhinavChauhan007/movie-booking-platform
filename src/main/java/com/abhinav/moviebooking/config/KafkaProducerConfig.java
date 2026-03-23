package com.abhinav.moviebooking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    /**
     * ObjectMapper for JSON serialization of events
     * <p>
     * Configured to handle:
     * - Java 8 time types (Instant) used in BookingEvent.timestamp
     * - Proper date formatting (ISO-8601 instead of timestamps)
     * <p>
     * This is similar to Redis configuration for consistency
     */
    @Bean
    public ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register module for Java 8 time types (Instant, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Serialize dates as ISO-8601 strings instead of numeric timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    /**
     * 33      * Configure Kafka Producer properties
     * 34      *
     * 35      * Key configurations:
     * 36      * - BOOTSTRAP_SERVERS: Kafka broker address (localhost:9092)
     * 37      * - KEY_SERIALIZER: String keys for event identification
     * 38      * - VALUE_SERIALIZER: JSON for event objects
     * 39      * - ACKS_CONFIG: "all" ensures message is written to all replicas (durability)
     * 40      * - RETRIES_CONFIG: Retry 3 times on transient failures
     * 41
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper kafkaObjectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability settings from application.properties
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(configProps);
        factory.setValueSerializer(new JsonSerializer<>(kafkaObjectMapper));
        return factory;
    }


    /**
     * KafkaTemplate bean for publishing events
     * <p>
     * This is the main Spring abstraction for sending messages to Kafka.
     * It will be injected into EventPublisher service.
     *
     * @return KafkaTemplate configured with JSON serialization
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }


}
