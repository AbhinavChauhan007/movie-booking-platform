package com.abhinav.moviebooking.booking.persistence.adapter;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingConcurrencyException;
import com.abhinav.moviebooking.booking.exception.BookingNotFoundException;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import com.abhinav.moviebooking.booking.persistence.mapper.BookingMapper;
import com.abhinav.moviebooking.booking.persistence.repository.BookingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class BookingPersistenceAdapter {

    private final BookingRepository bookingRepository;
    private final EntityManager entityManager;

    public BookingPersistenceAdapter(BookingRepository bookingRepository, EntityManager entityManager) {
        this.bookingRepository = bookingRepository;
        this.entityManager = entityManager;
    }

    /**
     * Save or update a booking in the database.
     * Uses a managed entity to avoid merge and prevent optimistic locking issues.
     */
    @Transactional
    public Booking save(Booking booking) {
        try {
            BookingEntity entity;

            if (booking.getBookingId() != null) {
                // Existing booking → fetch managed entity from DB
                entity = entityManager.find(BookingEntity.class, booking.getBookingId());
                if (entity == null) {
                    throw new BookingNotFoundException(booking.getBookingId());
                }

                // Update managed entity with domain state
                BookingMapper.updateEntityFromDomain(booking, entity);

            } else {
                // New booking → create entity and persist
                entity = BookingMapper.toEntity(booking);
                entityManager.persist(entity);
                booking.assignId(entity.getBookingId());
            }

            // Force sync with DB (optional)
            entityManager.flush();

            return booking;

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingConcurrencyException(booking.getBookingId());
        }
    }

    /**
     * Find a booking by ID and return domain object.
     */
    public Optional<Booking> findDomainById(Long id) {
        return bookingRepository.findById(id)
                .map(BookingMapper::toDomain);
    }

    public Optional<Booking> findDomainByIdWithLock(Long id) {
        BookingEntity entity = entityManager.find(BookingEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        return Optional.ofNullable(entity).map(BookingMapper::toDomain);
    }

    /**
     * Fetch expired initiated bookings in batches for processing.
     */
    public List<Long> findExpiredInitiatedBookings(Instant expiryTime, int batchSize) {
        return bookingRepository.findExpiredInitiatedBookings(
                BookingStatus.INITIATED,
                expiryTime,
                PageRequest.of(0, batchSize)
        );
    }
}
