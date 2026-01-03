package com.abhinav.moviebooking.movie.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class MovieRequestDTO {

    @NotBlank(message = "Title must not be empty")
    private String title;

    @NotBlank(message = "Genre must not be empty")
    private String genre;

    @Min(value = 1,message = "Duration must be at least 1 minute")
    private int durationMinutes;

    public MovieRequestDTO() {
    }

    public MovieRequestDTO(String title, String genre, int durationMinutes) {
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
