package com.abhinav.moviebooking.event.listener;

import com.abhinav.moviebooking.event.BookingCancelledEvent;
import com.abhinav.moviebooking.event.BookingConfirmedEvent;
import com.abhinav.moviebooking.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Listener service for booking events from Kafka
 * <p>
 * This service consumes events and can trigger:
 * - Email/SMS notifications
 * - Analytics processing
 * - Audit logging
 * - Webhooks to external systems
 */
@Service
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);

    @KafkaListener(
            topics = "${kafka.topic.booking-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBookingEvent(
            @Payload BookingEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            logger.info(" Received event: type={}, bookingId={}, userId={}, partition={}, offset={}",
                    event.getEventType(), event.getBookingId(), event.getUserId(), partition, offset);

            // Process based on event type
            if (event instanceof BookingConfirmedEvent confirmedEvent) {
                handleBookingConfirmed(confirmedEvent);
            } else if (event instanceof BookingCancelledEvent cancelledEvent) {
                handleBookingCancelled(cancelledEvent);
            } else {
                logger.warn("Unknown event type: {}", event.getEventType());
            }

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing booking event: bookingId={}, eventType={}",
                    event.getBookingId(), event.getEventType(), e);
            throw e;
        }
    }

    private void handleBookingConfirmed(BookingConfirmedEvent event) {
        logger.info("Processing BOOKING_CONFIRMED: bookingId={}, movieTitle={}, seats={}, price={}",
                event.getBookingId(), event.getMovieTitle(), event.getSeatNumbers(), event.getTotalPrice());

        // TODO: Send confirmation email/SMS
        // TODO: Update analytics
        // TODO: Trigger webhooks
    }

    private void handleBookingCancelled(BookingCancelledEvent event) {
        logger.info("Processing BOOKING_CANCELLED: bookingId={}, reason={}",
                event.getBookingId(), event.getCancellationReason());

        // TODO: Send cancellation notification
        // TODO: Update analytics
    }
}
