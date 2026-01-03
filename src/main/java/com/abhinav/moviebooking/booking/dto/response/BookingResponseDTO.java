package com.abhinav.moviebooking.booking.dto.response;

public class BookingResponseDTO {

    private Long bookingId;

    private String status;

    private String message;

    public BookingResponseDTO(Long bookingId, String status, String message) {
        this.bookingId = bookingId;
        this.status = status;
        this.message = message;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
