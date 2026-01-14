package com.abhinav.moviebooking.booking.dto.request;

import com.abhinav.moviebooking.booking.seat.SeatType;
import jakarta.validation.constraints.NotNull;

public class BookingWorkflowRequestDTO {

    @NotNull
    private Long bookingId;

    @NotNull
    private Long showId;

    @NotNull
    private int seatCount;

    @NotNull
    private SeatType seatType;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }
}
