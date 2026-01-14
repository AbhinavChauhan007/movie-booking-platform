package com.abhinav.moviebooking.booking.persistence.adapter;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingConcurrencyException;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import com.abhinav.moviebooking.booking.persistence.mapper.BookingMapper;
import com.abhinav.moviebooking.booking.persistence.repository.BookingRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class BookingPersistenceAdapter {

    private final BookingRepository bookingRepository;

    public BookingPersistenceAdapter(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking save(Booking booking) {
        try {
            BookingEntity savedBookingEntity = bookingRepository.save(BookingMapper.toEntity(booking));

            // assign ID back to domain if newly created
            if (booking.getBookingId() == null) {
                booking.assignId(savedBookingEntity.getBookingId());
            }
            return booking;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingConcurrencyException(booking.getBookingId());
        }
    }

    public Optional<Booking> findDomainById(Long id) {
        return bookingRepository.findById(id)
                .map(BookingMapper::toDomain);
    }

    public List<Booking> findExpiredInitiatedBookings(Instant expiryTime) {
        return bookingRepository.findExpiredInitiatedBookings(
                        BookingStatus.INITIATED,
                        expiryTime
                ).stream()
                .map(BookingMapper::toDomain)
                .toList();
    }
}
