package com.abhinav.moviebooking.booking.persistence.entity;

import com.abhinav.moviebooking.booking.cancellation.BookingCancellationReason;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "bookings")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booking_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason")
    private BookingCancellationReason cancellationReason;

    @Column(name = "total_price")
    private Double totalPrice;

    public BookingEntity() {
        // JPA only
    }

    public BookingEntity(Long bookingId, Long userId, BookingStatus bookingStatus, Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalStateException("createdAt must be set by domain");
        }

        if (userId == null) {
            throw new IllegalStateException("userId must be set");
        }
        this.bookingId = bookingId;
        this.userId = userId;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
    }

    public BookingEntity(Long bookingId,
                         Long userId,
                         BookingStatus bookingStatus,
                         Instant createdAt,
                         Instant updatedAt) {

        if (createdAt == null) {
            throw new IllegalStateException("createdAt must not be null");
        }

        if (userId == null) {
            throw new IllegalStateException("userId must not be null");
        }

        this.bookingId = bookingId;
        this.userId = userId;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
    }

    @PrePersist
    void onCreate() {
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BookingCancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(BookingCancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

}
