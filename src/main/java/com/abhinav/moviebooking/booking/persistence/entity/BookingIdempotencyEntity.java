package com.abhinav.moviebooking.booking.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "booking_idempotency")
public class BookingIdempotencyEntity {

    @Id
    private String idempotencyKey;

    private Long bookingId;

    private Instant createdDate;

    public BookingIdempotencyEntity() {}

    @PrePersist
    public void prePersist() {
        createdDate = Instant.now();
    }

    public BookingIdempotencyEntity(String idempotencyKey, Long bookingId) {
        this.idempotencyKey = idempotencyKey;
        this.bookingId = bookingId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
