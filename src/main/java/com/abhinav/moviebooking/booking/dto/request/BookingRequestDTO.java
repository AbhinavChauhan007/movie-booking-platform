package com.abhinav.moviebooking.booking.dto.request;

import com.abhinav.moviebooking.booking.seat.strategy.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BookingRequestDTO {

    @NotNull
    private Long bookingId;

    @NotNull
    private Long showId;

    @Min(1)
    private Integer seatCount;

    private SeatType seatType;

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
}
