package com.abhinav.moviebooking.show.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "show")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long movieId;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Integer screenNumber;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    protected Show() {}

    public Show(Long movieId, Instant startTime, Integer screenNumber, Integer totalSeats) {
        this.movieId = movieId;
        this.startTime = startTime;
        this.screenNumber = screenNumber;
        this.totalSeats = totalSeats;
    }

    // getters only (immutability is GOOD here)


    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Integer getScreenNumber() {
        return screenNumber;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getDeactivatedAt() {
        return deactivatedAt;
    }

    public void deactivate() {
        this.active = false;
        this.deactivatedAt = Instant.now();
    }

    public void updateStartTime(Instant newStartTime) {
        this.startTime = newStartTime;
    }

    public void updateScreenNumber(Integer newScreenNumber) {
        this.screenNumber = newScreenNumber;
    }


}

