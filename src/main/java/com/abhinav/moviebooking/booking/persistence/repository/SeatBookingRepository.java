package com.abhinav.moviebooking.booking.persistence.repository;

import com.abhinav.moviebooking.booking.persistence.entity.SeatBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatBookingRepository extends JpaRepository<SeatBookingEntity, Long> {

    @Query("""
            SELECT s.seatNumber
            FROM SeatBookingEntity s
            WHERE s.showId = :showId
            """
    )
    List<String> findBookedSeats(@Param("showId") Long showId);

    List<SeatBookingEntity> findAllByBookingId(Long bookingId);
}
