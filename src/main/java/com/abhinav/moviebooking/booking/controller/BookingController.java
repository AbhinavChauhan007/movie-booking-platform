package com.abhinav.moviebooking.booking.controller;

import com.abhinav.moviebooking.booking.domain.Booking;
import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.dto.request.BookingRequestDTO;
import com.abhinav.moviebooking.booking.dto.response.BookingHistoryDTO;
import com.abhinav.moviebooking.booking.dto.response.BookingResponseDTO;
import com.abhinav.moviebooking.booking.facade.BookingFacade;
import com.abhinav.moviebooking.booking.service.BookingHistoryService;
import com.abhinav.moviebooking.common.dto.ApiResponse;
import com.abhinav.moviebooking.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
            summary = "Get booking status",
            description = "Retrieve the current status of a specific booking"
    )
    public ResponseEntity<BookingResponseDTO> getBookingStatus(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Verify booking belongs to user (unless admin)
        if (userDetails.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            bookingFacade.verifyBookingOwnership(bookingId, userDetails.getUserId());
        }
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
            summary = "Cancel a booking",
            description = "Cancel an existing booking and release the seats"
    )
    public ResponseEntity<BookingResponseDTO> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Verify booking belongs to user (unless admin)
        if (userDetails.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            bookingFacade.verifyBookingOwnership(bookingId, userDetails.getUserId());
        }
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Expire a booking (Admin only)",
            description = "Mark a booking as expired - restricted to administrators for cleanup operations"
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
            summary = "Get user booking history (paginated)",
            description = "Retrieve all bookings made by the authenticated user with pagination and optional status filter. " +
                    "Filter by status: CREATED, INITIATED, CONFIRMED, CANCELLED, EXPIRED"
    )
    public ResponseEntity<ApiResponse<Page<BookingHistoryDTO>>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(required = false) String status
    ) {
        Long userId = userDetails.getUserId();

        // Parse sort parameters
        Sort.Order order = sort.length > 1
                ? new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0])
                : new Sort.Order(Sort.Direction.DESC, sort[0]);

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<BookingHistoryDTO> bookingHistory = bookingHistoryService.getUserBookingHistory(
                userId, pageable, status
        );

        return ResponseEntity.ok(
                ApiResponse.success("Booking history retrieved successfully", bookingHistory)
        );
    }


}
