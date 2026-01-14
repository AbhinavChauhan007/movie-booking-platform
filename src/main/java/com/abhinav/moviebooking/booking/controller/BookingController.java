package com.abhinav.moviebooking.booking.controller;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.dto.request.BookingRequestDTO;
import com.abhinav.moviebooking.booking.dto.request.BookingWorkflowRequestDTO;
import com.abhinav.moviebooking.booking.dto.response.BookingResponseDTO;
import com.abhinav.moviebooking.booking.facade.BookingFacade;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingFacade bookingFacade;

    public BookingController(BookingFacade bookingFacade) {
        this.bookingFacade = bookingFacade;
    }

    /**
     * Initiates a booking by bookingId.
     * Delegates to InitiatedState via BookingFacade.
     */
    @PostMapping("/initiate")
    public ResponseEntity<BookingResponseDTO> initiateBooking(@RequestBody @Valid BookingRequestDTO requestDTO) {
        Booking booking = bookingFacade.initiateBooking(
                requestDTO.getShowId(),
                requestDTO.getSeatCount(),
                requestDTO.getSeatType(),
                requestDTO.getIdempotencyKey()
        );
        return ResponseEntity.ok(
                new BookingResponseDTO(
                        booking.getBookingId(),
                        booking.getBookingStatus().name(),
                        "Booking initiated successfully"
                )
        );
    }

    /**
     * Optional: fetch booking status.
     * Can call BookingService or extend facade later.
     */
    @GetMapping("/status/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingStatus(@PathVariable Long bookingId) {
        BookingStatus status = bookingFacade.getStatus(bookingId);
        return ResponseEntity.ok(
                new BookingResponseDTO(
                        bookingId,
                        status.name(),
                        "Booking status fetched successfully"
                )
        );
    }

    /**
     * Cancel booking
     */
    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Long bookingId) {
        Booking booking = bookingFacade.cancelBooking(bookingId);
        return ResponseEntity.ok(
                new BookingResponseDTO(
                        booking.getBookingId(),
                        booking.getBookingStatus().name(),
                        "Booking cancelled successfully"
                )
        );
    }

    /**
     * expire booking
     */
    @PostMapping("/expire/{bookingId}")
    public ResponseEntity<BookingResponseDTO> expireBooking(@PathVariable Long bookingId) {
        Booking booking = bookingFacade.expireBooking(bookingId);
        return ResponseEntity.ok(
                new BookingResponseDTO(
                        booking.getBookingId(),
                        booking.getBookingStatus().name(),
                        "Booking expired successfully"
                )
        );
    }


}
