package com.abhinav.moviebooking.show.controller;

import com.abhinav.moviebooking.show.dto.CreateShowRequest;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.service.ShowService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/shows")
@PreAuthorize("hasRole('ADMIN')")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping("/createShow")
    public Show createShow(@RequestBody CreateShowRequest request) {
        return showService.createShow(request);
    }
}

