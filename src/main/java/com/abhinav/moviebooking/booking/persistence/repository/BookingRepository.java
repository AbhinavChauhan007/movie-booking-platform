package com.abhinav.moviebooking.booking.persistence.repository;

import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    @Query("""
             SELECT b.bookingId FROM BookingEntity b
             WHERE b.bookingStatus = :status
             AND b.createdAt < :expiryTime
            """)
        // Removed @Lock and changed return type to List<Long>
    List<Long> findExpiredInitiatedBookings(
            @Param("status") BookingStatus bookingStatus,
            @Param("expiryTime") Instant expiryTime,
            Pageable pageable
    );

    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""                                                                                                                                                                                 
            SELECT COUNT(b) > 0 FROM BookingEntity b
            JOIN SeatBookingEntity sb ON b.bookingId = sb.bookingId
            WHERE sb.showId = :showId
            AND b.bookingStatus IN ('INITIATED', 'CONFIRMED')
            """)
    boolean existsActiveBookingsByShowId(@Param("showId") Long showId);
}

