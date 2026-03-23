package com.abhinav.moviebooking.booking.persistence.mapper;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;

public final class BookingMapper {

    private BookingMapper() {
        // utility class
    }

    /**
     * Convert a domain Booking to a new BookingEntity (for persist).
     */
    public static BookingEntity toEntity(Booking booking) {
        BookingEntity entity = new BookingEntity(
                booking.getBookingId(),
                booking.getUserId(),
                booking.getBookingStatus(),
                booking.getCreatedAt()
        );

        if (booking.getCancellationReason() != null) {
            entity.setCancellationReason(booking.getCancellationReason());
        }

        // Extract totalPrice from execution context if available
        if (booking.getBookingExecutionContext() != null) {
            entity.setTotalPrice(booking.getBookingExecutionContext().getFinalPrice());
        }

        return entity;
    }

    /**
     * Convert a BookingEntity from DB to domain Booking.
     */
    public static Booking toDomain(BookingEntity entity) {
        return Booking.rehydrate(
                entity.getBookingId(),
                entity.getUserId(),
                entity.getBookingStatus(),
                entity.getCreatedAt(),
                entity.getCancellationReason()
        );
    }

    /**
     * Update an existing managed BookingEntity with the state from a domain Booking.
     * This is used to avoid merging detached entities.
     */
    public static void updateEntityFromDomain(Booking domain, BookingEntity entity) {
        entity.setBookingStatus(domain.getBookingStatus());
        entity.setCancellationReason(domain.getCancellationReason());

        // Update price from execution context if available
        if (domain.getBookingExecutionContext() != null) {
            entity.setTotalPrice(domain.getBookingExecutionContext().getFinalPrice());
        }
        // userId should never change (immutable business key)
        // createdAt should never change
        // updatedAt is handled automatically by @PreUpdate
    }
}
