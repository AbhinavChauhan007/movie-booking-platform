package com.abhinav.moviebooking.show.controller;

import com.abhinav.moviebooking.show.dto.CreateShowRequest;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public Show createShow(@RequestBody CreateShowRequest request) {
        return showService.createShow(request);
    }
}

