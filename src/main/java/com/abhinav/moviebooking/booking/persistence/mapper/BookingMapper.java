package com.abhinav.moviebooking.booking.persistence.mapper;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingEntity toEntity(Booking booking) {
        return new BookingEntity(
                booking.getBookingId(),
                booking.getBookingStatus()
        );
    }

    public static Booking toDomain(BookingEntity bookingEntity) {
        return new Booking(
                bookingEntity.getBookingId(),
                bookingEntity.getBookingStatus()
        );
    }
}
