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
                booking.getBookingStatus(),
                booking.getCreatedAt()
        );

        if (booking.getCancellationReason() != null) {
            entity.setCancellationReason(booking.getCancellationReason());
        }

        return entity;
    }

    /**
     * Convert a BookingEntity from DB to domain Booking.
     */
    public static Booking toDomain(BookingEntity entity) {
        return Booking.rehydrate(
                entity.getBookingId(),
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
        // createdAt should never change
        // updatedAt is handled automatically by @PreUpdate
    }
}
