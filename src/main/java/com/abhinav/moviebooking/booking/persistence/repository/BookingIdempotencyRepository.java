package com.abhinav.moviebooking.booking.persistence.repository;

import com.abhinav.moviebooking.booking.persistence.entity.BookingIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingIdempotencyRepository extends JpaRepository<BookingIdempotencyEntity, String> {
}
