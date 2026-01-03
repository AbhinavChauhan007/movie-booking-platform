package com.abhinav.moviebooking.booking.controller;

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

    private final StandardBookingWorkflow standardBookingWorkflow;

    public BookingController(BookingFacade bookingFacade, StandardBookingWorkflow standardBookingWorkflow) {
        this.bookingFacade = bookingFacade;
        this.standardBookingWorkflow = standardBookingWorkflow;
    }

    /**
     * Initiates a booking by bookingId.
     * Delegates to InitiatedState via BookingFacade.
     */
    @PostMapping("/initiate")
    public ResponseEntity<BookingResponseDTO> initiateBooking(@RequestBody @Valid BookingRequestDTO requestDTO) {
        bookingFacade.initiateBooking(
                requestDTO.getBookingId(),
                requestDTO.getShowId(),
                requestDTO.getSeatCount(),
                requestDTO.getSeatType()
        );
        return ResponseEntity.ok(
                new BookingResponseDTO(
                        requestDTO.getBookingId(),
                        "INITIATED",
                        "Booking initiated successfully"
                )
        );
    }

    /**
     * Confirms a booking by bookingId.
     * Delegates to ConfirmedState via BookingFacade.
     */
    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable Long bookingId) {
        bookingFacade.confirmBooking(bookingId);
        return ResponseEntity.ok(new BookingResponseDTO(
                bookingId,
                "CONFIRMED",
                "Booking confirmed successfully"
        ));
    }


    @PostMapping("/execute")
    public ResponseEntity<BookingResponseDTO> executeBooking(@RequestBody @Valid BookingWorkflowRequestDTO requestDTO) {
        // 1. Initialize workflow with request DTO
        standardBookingWorkflow.init(
                requestDTO.getSeatType(),
                requestDTO.getSeatCount(),
                requestDTO.getShowId(),
                requestDTO.getBookingId()
        );

        // 2. Execute all steps: validate, allocateSeats, calculatePrice, payment, confirmBooking
        standardBookingWorkflow.execute();

        // 3. Return response
        return ResponseEntity.ok(
                new BookingResponseDTO(
                        requestDTO.getBookingId(),
                        "CONFIRMED",
                        "Booking executed successfully via workflow"
                )
        );
    }

    /**
     * Optional: fetch booking status.
     * Can call BookingService or extend facade later.
     */
    @GetMapping("/status/{bookingId}")
    public ResponseEntity<String> getBookingStatus(@PathVariable Long bookingId) {
        // TODO: integrate with BookingService to fetch real status
        return ResponseEntity.ok("Booking status for ID " + bookingId + " is TBD");
    }

    /**
     * Placeholder for cancel booking.
     * If we add CANCELLED state tomorrow, this method will delegate
     * to the CANCELLEDState via BookingFacade (no changes in facade structure needed yet).
     */
    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        // TODO: implement CANCELLEDState and add facade method
        return ResponseEntity.status(501).body("Cancel functionality not implemented yet");
    }


}
