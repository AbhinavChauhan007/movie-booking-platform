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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/shows")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Shows (Admin)", description = "Movie show scheduling and management (Admin only)")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping("/createShow")
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

    @GetMapping("/getAllShows")
    @Operation(
            summary = "Get all active shows",
            description = "Retrieve all currently active shows for management purposes"
    )
    public ResponseEntity<ApiResponse<List<Show>>> getAllActiveShows() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Shows retrieved successfully",
                        showService.getAllActiveShows()
                )
        );
    }

    @GetMapping("/getShow/{id}")
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

    @GetMapping("/getUpcomingShows")
    @Operation(
            summary = "Get future shows",
            description = "Retrieve all upcoming shows that are available for booking"
    )
    public ResponseEntity<ApiResponse<List<Show>>> getUpcomingShows() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Future shows retrieved successfully",
                        showService.getFutureShows()
                )
        );
    }

    @PutMapping("/updateShow/{id}")
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

