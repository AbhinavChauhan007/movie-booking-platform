package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingConcurrencyException;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingPersistenceAdapterTest {

    private BookingRepository bookingRepository;
    private BookingPersistenceAdapter bookingPersistenceAdapter;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        bookingPersistenceAdapter = new BookingPersistenceAdapter(bookingRepository);
    }

    @Test
    @DisplayName("Should save booking and assign ID if new")
    void shouldSaveAndAssignId() {
        Booking booking = Booking.newBooking();

        BookingEntity savedEntity = new BookingEntity(
                1L,
                BookingStatus.CREATED,
                booking.getCreatedAt(),
                booking.getCreatedAt()
        );

        when(bookingRepository.save(any(BookingEntity.class)))
                .thenReturn(savedEntity);

        Booking savedBooking = bookingPersistenceAdapter.save(booking);

        assertEquals(1L, savedBooking.getBookingId());
        verify(bookingRepository).save(any(BookingEntity.class));
    }

    @Test
    @DisplayName("Should throw BookingConcurrencyException on optimistic lock failure")
    void shouldThrowConcurrencyException() {
        Booking booking = Booking.newBooking();
        booking.assignId(10L);

        when(bookingRepository.save(any()))
                .thenThrow(ObjectOptimisticLockingFailureException.class);

        assertThrows(
                BookingConcurrencyException.class,
                () -> bookingPersistenceAdapter.save(booking)
        );
    }

    @Test
    @DisplayName("Should find booking by ID")
    void shouldFindById() {
        Instant now = Instant.now();

        BookingEntity entity = new BookingEntity(
                2L,
                BookingStatus.CONFIRMED,
                now,
                now
        );

        when(bookingRepository.findById(2L))
                .thenReturn(Optional.of(entity));

        Optional<Booking> result =
                bookingPersistenceAdapter.findDomainById(2L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getBookingId());
        assertEquals(BookingStatus.CONFIRMED, result.get().getBookingStatus());
        assertEquals(now, result.get().getCreatedAt());
    }

    @Test
    @DisplayName("Should return empty optional if booking not found")
    void shouldReturnEmptyIfNotFound() {
        when(bookingRepository.findById(3L))
                .thenReturn(Optional.empty());

        Optional<Booking> result =
                bookingPersistenceAdapter.findDomainById(3L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find expired initiated bookings")
    void shouldFindExpiredInitiatedBookings() {
        Instant createdAt = Instant.now().minusSeconds(3600);

        BookingEntity entity = new BookingEntity(
                5L,
                BookingStatus.INITIATED,
                createdAt,
                createdAt
        );

        when(bookingRepository.findExpiredInitiatedBookings(
                eq(BookingStatus.INITIATED),
                any(Instant.class)
        )).thenReturn(List.of(entity));

        List<Booking> result =
                bookingPersistenceAdapter.findExpiredInitiatedBookings(Instant.now());

        assertEquals(1, result.size());
        assertEquals(BookingStatus.INITIATED, result.get(0).getBookingStatus());
        assertEquals(createdAt, result.get(0).getCreatedAt());
    }
}
