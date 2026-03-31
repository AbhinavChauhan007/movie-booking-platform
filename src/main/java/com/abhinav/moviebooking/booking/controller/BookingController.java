package com.abhinav.moviebooking.booking.controller;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.dto.request.BookingRequestDTO;
import com.abhinav.moviebooking.booking.dto.request.BookingWorkflowRequestDTO;
import com.abhinav.moviebooking.booking.dto.response.BookingHistoryDTO;
import com.abhinav.moviebooking.booking.dto.response.BookingResponseDTO;
import com.abhinav.moviebooking.booking.facade.BookingFacade;
import com.abhinav.moviebooking.booking.service.BookingHistoryService;
import com.abhinav.moviebooking.booking.workflow.impl.StandardBookingWorkflow;
import com.abhinav.moviebooking.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Movie ticket booking operations")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingFacade bookingFacade;
    private final BookingHistoryService bookingHistoryService;

    public BookingController(BookingFacade bookingFacade, BookingHistoryService bookingHistoryService) {
        this.bookingFacade = bookingFacade;
        this.bookingHistoryService = bookingHistoryService;
    }

    /**
     * Initiates a booking by bookingId.
     * Delegates to InitiatedState via BookingFacade.
     */
    @PostMapping("/initiateBooking")
    @Operation(
            summary = "Create a new booking",
            description = "Initiates a new movie ticket booking with seat allocation, pricing, and payment processing"
    )
    public ResponseEntity<BookingResponseDTO> initiateBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,  // NEW - Spring injects authenticated user
            @RequestBody @Valid BookingRequestDTO requestDTO) {

        Long userId = userDetails.getUserId();
        Booking booking = bookingFacade.initiateBooking(
                userId,
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
    @Operation(
            summary = "Get booking status",
            description = "Retrieve the current status of a specific booking"
    )
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
    @Operation(
            summary = "Cancel a booking",
            description = "Cancel an existing booking and release the seats"
    )
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
    @Operation(
            summary = "Expire a booking",
            description = "Mark a booking as expired (typically for admin cleanup)"
    )
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

    @GetMapping("/my-bookings")
    @Operation(
            summary = "Get user booking history",
            description = "Retrieve all bookings made by the authenticated user"
    )
    public ResponseEntity<List<BookingHistoryDTO>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        List<BookingHistoryDTO> bookingHistory = bookingHistoryService.getUserBookingHistory(userId);
        return ResponseEntity.ok(bookingHistory);
    }


}
