package com.abhinav.moviebooking.event.service;

import com.abhinav.moviebooking.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing booking events to Kafka
 * <p>
 * This service abstracts Kafka publishing logic and provides
 * a simple interface for other services to publish events.
 */
@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String bookingEventsTopic;

    public EventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topic.booking-events}") String bookingEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.bookingEventsTopic = bookingEventsTopic;
    }

    /**
     * Publishes a booking event to Kafka
     *
     * @param event The booking event to publish (BookingConfirmedEvent or BookingCancelledEvent)
     */
    public void publishEvent(BookingEvent event) {
        try {
            // Use bookingId as the message key for consistent partitioning
            String key = String.valueOf(event.getBookingId());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(bookingEventsTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to publish event: eventType={}, bookingId={}, error={}",
                            event.getEventType(), event.getBookingId(), ex.getMessage(), ex);
                } else {
                    logger.info("Event published: eventType={}, bookingId={}, partition={}, offset={}",
                            event.getEventType(),
                            event.getBookingId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            logger.error("Exception while publishing event: eventType={}, bookingId={}",
                    event.getEventType(), event.getBookingId(), e);
        }
    }

    /**
     * Publishes event synchronously - useful for testing or critical events
     */
    public void publishEventSync(BookingEvent event) throws Exception{
        String key = String.valueOf(event.getBookingId());

        SendResult<String, Object> result = kafkaTemplate.send(bookingEventsTopic,key,event).get();
        logger.info("Event published synchronously: eventType={}, bookingId={}, partition={}, offset={}",
                event.getEventType(),
                event.getBookingId(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }
}
