package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.event.BookingConfirmedEvent;
import com.abhinav.moviebooking.event.BookingEvent;
import com.abhinav.moviebooking.event.service.EventPublisher;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6370",
        "grpc.server.port=0",
        "grpc.client.seat-service.address=static://localhost:9091",
        "grpc.client.pricing-service.address=static://localhost:9091"
})
@DirtiesContext
@EmbeddedKafka(partitions = 3, topics = {"booking-events"})
class EventPublisherIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Value("${kafka.topic.booking-events}")
    private String bookingEventsTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Test
    void shouldPublishBookingConfirmedEvent() throws Exception {
        // Setup consumer
        BlockingQueue<ConsumerRecord<String, BookingEvent>> records = new LinkedBlockingQueue<>();

        ContainerProperties containerProps = new ContainerProperties(bookingEventsTopic);
        containerProps.setMessageListener((MessageListener<String, BookingEvent>) records::add);

        KafkaMessageListenerContainer<String, BookingEvent> container =
                new KafkaMessageListenerContainer<>(createConsumerFactory(), containerProps);

        container.start();
        ContainerTestUtils.waitForAssignment(container, 3);

        // Publish event
        BookingConfirmedEvent event = new BookingConfirmedEvent(
                123L, 456L, 789L, "Inception", List.of("A1", "A2"), 500.0
        );
        eventPublisher.publishEventSync(event);

        // Verify
        ConsumerRecord<String, BookingEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo("123");
        assertThat(received.value()).isInstanceOf(BookingConfirmedEvent.class);

        BookingConfirmedEvent receivedEvent = (BookingConfirmedEvent) received.value();
        assertThat(receivedEvent.getBookingId()).isEqualTo(123L);
        assertThat(receivedEvent.getMovieTitle()).isEqualTo("Inception");

        container.stop();
    }

    private DefaultKafkaConsumerFactory<String, BookingEvent> createConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(BookingEvent.class));
    }
}
