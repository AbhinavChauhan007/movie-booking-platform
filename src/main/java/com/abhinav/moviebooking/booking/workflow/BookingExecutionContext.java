package com.abhinav.moviebooking.booking.workflow;

import com.abhinav.moviebooking.booking.seat.strategy.SeatType;

import java.io.Serializable;
import java.util.List;

public class BookingExecutionContext implements Serializable {

    private Long showId;
    private int seatCount;
    private SeatType seatType;
    private double finalPrice;

    private List<String> allocatedSeats;

    public BookingExecutionContext() {
    }

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
