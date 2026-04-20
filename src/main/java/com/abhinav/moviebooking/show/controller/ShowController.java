package com.abhinav.moviebooking.show.controller;

import com.abhinav.moviebooking.common.dto.ApiResponse;
import com.abhinav.moviebooking.show.dto.CreateShowRequest;
import com.abhinav.moviebooking.show.dto.UpdateShowRequestDTO;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/admin/shows")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Shows (Admin)", description = "Movie show scheduling and management (Admin only)")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping("/createShow")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create show",
            description = "Schedule a new movie show with start time, screen number, and total seats"
    )
    public ResponseEntity<ApiResponse<Show>> createShow(@RequestBody @Valid CreateShowRequest request) {
        Show show = showService.createShow(request);
        return ResponseEntity.
                status(HttpStatus.CREATED).
                body(
                        ApiResponse.success(
                                "Show with Id " + show.getId() + " created successfully", show
                        )
                );
    }

    // GET /admin/shows/getAllShows - Admin version (paginated)
    @GetMapping("/getAllShows")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(
            summary = "Get all shows (paginated)",
            description = "Retrieve shows with pagination and filters for admin management"
    )
    public ResponseEntity<ApiResponse<Page<Show>>> getAllShows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime,asc") String[] sort,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) Integer screenNumber,
            @RequestParam(required = false) Boolean futureOnly
    ) {
        Sort.Order order = sort.length > 1
                ? new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0])
                : new Sort.Order(Sort.Direction.ASC, sort[0]);

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<Show> shows = showService.getAllActiveShows(pageable, movieId, startDate, endDate,
                screenNumber, futureOnly);

        return ResponseEntity.ok(
                ApiResponse.success("Shows retrieved successfully", shows)
        );
    }

    @GetMapping("/getShow/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(
            summary = "Get show by ID",
            description = "Retrieve detailed information about a specific show"
    )
    public ResponseEntity<ApiResponse<Show>> getShowById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Show retrieved successfully",
                        showService.getShowById(id)
                )
        );
    }

    @PutMapping("/updateShow/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update show",
            description = "Update show start time and screen number. Only allowed if no bookings exist for the show."
    )
    public ResponseEntity<ApiResponse<Show>> updateShow(
            @PathVariable Long id,
            @RequestBody @Valid UpdateShowRequestDTO request) {
        Show updatedShow = showService.updateShow(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Show with ID " + id + " updated successfully",
                        updatedShow
                )
        );
    }

    @DeleteMapping("/deleteShow/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cancel show",
            description = "Cancel a scheduled show (soft delete). Note: Handle existing bookings separately if needed."
    )
    public ResponseEntity<ApiResponse<Void>> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.ok(
                ApiResponse.success("Show with ID " + id + " cancelled successfully")
        );
    }
}