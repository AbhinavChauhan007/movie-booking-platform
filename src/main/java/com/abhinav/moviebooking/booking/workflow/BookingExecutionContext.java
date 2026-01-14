package com.abhinav.moviebooking.booking.workflow;

import com.abhinav.moviebooking.booking.seat.SeatType;

public class BookingExecutionContext {

    private final Long showId;
    private final int seatCount;
    private final SeatType seatType;

    private double finalPrice;

    public BookingExecutionContext(Long showId, int seatCount, SeatType seatType) {
        this.showId = showId;
        this.seatCount = seatCount;
        this.seatType = seatType;
    }

    public Long getShowId() {
        return showId;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
