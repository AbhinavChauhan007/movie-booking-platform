package com.abhinav.moviebooking.booking.workflow;

import com.abhinav.moviebooking.booking.seat.strategy.SeatType;

import java.util.List;

public class BookingExecutionContext {

    private final Long showId;
    private final int seatCount;
    private final SeatType seatType;
    private double finalPrice;

    private List<String> allocatedSeats;

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

    public List<String> getAllocatedSeats() {
        return allocatedSeats;
    }

    public void setAllocatedSeats(List<String> allocatedSeats) {
        this.allocatedSeats = allocatedSeats;
    }
}
