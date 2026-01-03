package com.abhinav.moviebooking.movie.dto.response;

public class MovieResponseDTO {

    private long id;

    private String title;

    private String genre;

    private int durationMinutes;

    public MovieResponseDTO() {
    }

    public MovieResponseDTO(long id, String title, String genre, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
