package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingConcurrencyException;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingPersistenceAdapterTest {

    private static final Long TEST_USER_ID = 999L;

    private BookingRepository bookingRepository;
    private BookingPersistenceAdapter bookingPersistenceAdapter;
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        entityManager = mock(EntityManager.class);
        bookingPersistenceAdapter = new BookingPersistenceAdapter(bookingRepository, entityManager);
    }

    @Test
    @DisplayName("Should save new booking and assign ID")
    void shouldSaveNewBooking() {
        // Given
        Booking newBooking = Booking.newBooking(TEST_USER_ID);

        BookingEntity savedEntity = new BookingEntity(1L, TEST_USER_ID, BookingStatus.CREATED, Instant.now(), Instant.now());

        when(entityManager.find(BookingEntity.class, null)).thenReturn(null);

        // When
        Booking result = bookingPersistenceAdapter.save(newBooking);

        // Then
        verify(entityManager).persist(any(BookingEntity.class));
        verify(entityManager).flush();
    }

    @Test
    @DisplayName("Should update existing booking")
    void shouldUpdateExistingBooking() {
        // Given
        Long bookingId = 100L;
        Booking existingBooking = Booking.newBooking(TEST_USER_ID);
        existingBooking.assignId(bookingId);
        existingBooking.transitionTo(BookingStatus.INITIATED);

        BookingEntity entity = new BookingEntity(bookingId, TEST_USER_ID, BookingStatus.CREATED, Instant.now(), Instant.now());

        when(entityManager.find(BookingEntity.class, bookingId)).thenReturn(entity);

        // When
        Booking result = bookingPersistenceAdapter.save(existingBooking);

        // Then
        assertEquals(bookingId, result.getBookingId());
        verify(entityManager).find(BookingEntity.class, bookingId);
        verify(entityManager).flush();
        verify(entityManager, never()).persist(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent booking")
    void shouldThrowExceptionWhenUpdatingNonExistentBooking() {
        // Given
        Long bookingId = 999L;
        Booking booking = Booking.newBooking(TEST_USER_ID);
        booking.assignId(bookingId);

        when(entityManager.find(BookingEntity.class, bookingId)).thenReturn(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> bookingPersistenceAdapter.save(booking));
        verify(entityManager).find(BookingEntity.class, bookingId);
    }

    @Test
    @DisplayName("Should find booking by ID")
    void shouldFindBookingById() {
        // Given
        Long bookingId = 100L;
        Instant now = Instant.now();
        BookingEntity entity = new BookingEntity(bookingId, TEST_USER_ID, BookingStatus.INITIATED, now, now);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(entity));

        // When
        Optional<Booking> result = bookingPersistenceAdapter.findDomainById(bookingId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(bookingId, result.get().getBookingId());
        assertEquals(BookingStatus.INITIATED, result.get().getBookingStatus());
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    @DisplayName("Should return empty when booking not found by ID")
    void shouldReturnEmptyWhenBookingNotFound() {
        // Given
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When
        Optional<Booking> result = bookingPersistenceAdapter.findDomainById(bookingId);

        // Then
        assertFalse(result.isPresent());
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    @DisplayName("Should find booking by ID with Pessimistic Lock")
    void shouldFindByIdWithLock() {
        Instant now = Instant.now();
        Long bookingId = 100L;
        BookingEntity entity = new BookingEntity(bookingId, TEST_USER_ID, BookingStatus.INITIATED, now, now);

        // Mock entityManager.find with PESSIMISTIC_WRITE
        when(entityManager.find(eq(BookingEntity.class), eq(bookingId), eq(LockModeType.PESSIMISTIC_WRITE)))
                .thenReturn(entity);

        Optional<Booking> result = bookingPersistenceAdapter.findDomainByIdWithLock(bookingId);

        assertTrue(result.isPresent());
        assertEquals(bookingId, result.get().getBookingId());
        // Verify that the lock was actually requested
        verify(entityManager).find(any(), any(), eq(LockModeType.PESSIMISTIC_WRITE));
    }

    @Test
    @DisplayName("Should find only IDs of expired initiated bookings (Thin Query)")
    void shouldFindExpiredInitiatedBookingIds() {
        Instant threshold = Instant.now().minusSeconds(600);
        List<Long> expiredIds = List.of(10L, 20L, 30L);

        // Update mock to return List<Long> instead of List<BookingEntity>
        when(bookingRepository.findExpiredInitiatedBookings(
                eq(BookingStatus.INITIATED),
                eq(threshold),
                any(Pageable.class)
        )).thenReturn(expiredIds);

        List<Long> result = bookingPersistenceAdapter.findExpiredInitiatedBookings(threshold, 50);

        assertEquals(3, result.size());
        assertEquals(10L, result.get(0));
        assertEquals(20L, result.get(1));

        // Verify the repository was called with the correct status
        verify(bookingRepository).findExpiredInitiatedBookings(eq(BookingStatus.INITIATED), any(), any());
    }

    @Test
    @DisplayName("Should return empty list if no expired bookings exist")
    void shouldReturnEmptyListWhenNoExpired() {
        when(bookingRepository.findExpiredInitiatedBookings(any(), any(), any()))
                .thenReturn(List.of());

        List<Long> result = bookingPersistenceAdapter.findExpiredInitiatedBookings(Instant.now(), 50);

        assertTrue(result.isEmpty());
    }
}