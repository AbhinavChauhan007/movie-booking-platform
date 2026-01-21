package com.abhinav.moviebooking.booking.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "seat_booking",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"show_id", "seat_number"})
        }
)
public class SeatBookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    public SeatBookingEntity() {
    }

    public SeatBookingEntity(String seatNumber, Long showId, Long bookingId) {
        this.seatNumber = seatNumber;
        this.showId = showId;
        this.bookingId = bookingId;
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getShowId() {
        return showId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }
}
