package com.abhinav.moviebooking.booking.dto.response;

import java.time.Instant;
import java.util.List;

public class BookingHistoryDTO {

    private Long bookingId;
    private String status;
    private Long showId;
    private String movieTitle;
    private List<String> seats;
    private Double totalPrice;
    private Instant bookedAt;

    public BookingHistoryDTO() {
    }

    public BookingHistoryDTO(Long bookingId, String status, Long showId, String movieTitle, List<String> seats, Double totalPrice, Instant bookedAt) {
        this.bookingId = bookingId;
        this.status = status;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.bookedAt = bookedAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Instant getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Instant bookedAt) {
        this.bookedAt = bookedAt;
    }
}
