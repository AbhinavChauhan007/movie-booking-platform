package com.abhinav.moviebooking.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BookingConfirmedEvent.class, name = "BOOKING_CONFIRMED"),
        @JsonSubTypes.Type(value = BookingCancelledEvent.class, name = "BOOKING_CANCELLED")
})
public class BookingEvent {

    private Long bookingId;
    private Long userId;
    private String eventType;
    private Instant timestamp;

    public BookingEvent() {
        this.timestamp = Instant.now();
    }

    public BookingEvent(Long bookingId, Long userId, String eventType) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = Instant.now();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BookingEvent{" +
                "bookingId='" + bookingId + '\'' +
                ", userId=" + userId +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
