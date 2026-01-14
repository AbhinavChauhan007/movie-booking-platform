package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.exception.BookingConcurrencyException;
import com.abhinav.moviebooking.booking.persistence.adapter.BookingPersistenceAdapter;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import com.abhinav.moviebooking.booking.persistence.mapper.BookingMapper;
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
import static org.mockito.Mockito.*;

public class BookingPersistenceAdapterTest {

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
        Booking booking = new Booking();

        BookingEntity bookingEntity = BookingMapper.toEntity(booking);
        bookingEntity.setBookingId(1L);

        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(bookingEntity);
        Booking savedBooking = bookingPersistenceAdapter.save(booking);

        assertEquals(1L,savedBooking.getBookingId());
        verify(bookingRepository).save(any(BookingEntity.class));
    }

    @Test
    @DisplayName("Should throw BookingConcurrencyException on optimistic lock failure")
    void shouldThrowConcurrencyException() {
        Booking booking = new Booking();
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
        BookingEntity entity = new BookingEntity();
        entity.setBookingId(2L);
        entity.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(2L))
                .thenReturn(Optional.of(entity));

        Optional<Booking> result = bookingPersistenceAdapter.findDomainById(2L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getBookingId());
        assertEquals(BookingStatus.CONFIRMED, result.get().getBookingStatus());
    }

    @Test
    @DisplayName("Should return empty optional if booking not found")
    void shouldReturnEmptyIfNotFound() {
        when(bookingRepository.findById(3L))
                .thenReturn(Optional.empty());

        Optional<Booking> result = bookingPersistenceAdapter.findDomainById(3L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find expired initiated bookings")
    void shouldFindExpiredInitiatedBookings() {
        BookingEntity entity = new BookingEntity();
        entity.setBookingStatus(BookingStatus.INITIATED);

        when(bookingRepository.findExpiredInitiatedBookings(
                eq(BookingStatus.INITIATED),
                any(Instant.class)
        )).thenReturn(List.of(entity));

        List<Booking> result =
                bookingPersistenceAdapter.findExpiredInitiatedBookings(Instant.now());

        assertEquals(1, result.size());
        assertEquals(BookingStatus.INITIATED, result.get(0).getBookingStatus());
    }


}
