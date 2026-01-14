package com.abhinav.moviebooking.booking.persistence.repository;

import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    @Query("""
            SELECT b FROM BookingEntity b
                         WHERE b.bookingStatus = :status
                                     AND b.createdAt < :expiryTime
            """)
    List<BookingEntity> findExpiredInitiatedBookings(@Param("status") BookingStatus bookingStatus ,
                                                     @Param("expiryTime") Instant expiryTime);
}
