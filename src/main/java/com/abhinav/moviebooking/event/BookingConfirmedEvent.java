package com.abhinav.moviebooking.event;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when a booking is successfully confirmed.
 * Contains all details needed for sending a confirmation notification.
 */
public class BookingConfirmedEvent extends BookingEvent{

    private Long showId;
    private String movieTitle;
    private List<String> seatNumbers;
    private Double totalPrice;

    // Default constructor for JSON deserialization
    public BookingConfirmedEvent() {
        super();
    }

    public BookingConfirmedEvent(Long bookingId, Long userId, Long showId,
                                 String movieTitle, List<String> seatNumbers,
                                 Double totalPrice) {
        super(bookingId, userId, "BOOKING_CONFIRMED");
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.seatNumbers = seatNumbers;
        this.totalPrice = totalPrice;
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

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "BookingConfirmedEvent{" +
                "showId=" + showId +
                ", movieTitle='" + movieTitle + '\'' +
                ", seatNumbers=" + seatNumbers +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
