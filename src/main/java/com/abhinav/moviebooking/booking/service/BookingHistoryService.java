package com.abhinav.moviebooking.booking.service;

import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.dto.response.BookingHistoryDTO;
import com.abhinav.moviebooking.booking.exception.InvalidBookingStatusException;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import com.abhinav.moviebooking.booking.persistence.entity.SeatBookingEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingRepository;
import com.abhinav.moviebooking.booking.persistence.repository.SeatBookingRepository;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.show.entity.Show;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingHistoryService {

    private final BookingRepository bookingRepository;
    private final SeatBookingRepository seatBookingRepository;
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;


    public BookingHistoryService(BookingRepository bookingRepository, SeatBookingRepository seatBookingRepository, ShowRepository showRepository, MovieRepository movieRepository) {
        this.bookingRepository = bookingRepository;
        this.seatBookingRepository = seatBookingRepository;
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
    }

    public Page<BookingHistoryDTO> getUserBookingHistory(Long userId, Pageable pageable, String status) {
        Page<BookingEntity> bookingPage;

        // Fetch with or without status filter
        if (status != null && !status.isEmpty()) {
            // Validate status before converting
            BookingStatus bookingStatus;
            try {
                bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                String validValues = String.join(", ",
                        Arrays.stream(BookingStatus.values())
                                .map(Enum::name)
                                .toArray(String[]::new));

                throw new InvalidBookingStatusException(
                        String.format("'%s' is not a valid status. Accepted values: %s",
                                status, validValues)
                );
            }
            bookingPage = bookingRepository.findAllByUserIdAndBookingStatus(userId, bookingStatus, pageable);
        } else {
            bookingPage = bookingRepository.findAllByUserId(userId, pageable);
        }

        // Extract all booking IDs
        List<Long> bookingIds = bookingPage.getContent().stream()
                .map(BookingEntity::getBookingId)
                .toList();

        if (bookingIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Batch fetch all seat bookings for all bookings in one query
        List<SeatBookingEntity> allSeatBookings = seatBookingRepository.findAllByBookingIdIn(bookingIds);

        // Group seat bookings by bookingId for quick lookup
        Map<Long, List<SeatBookingEntity>> seatBookingsByBookingId = allSeatBookings.stream()
                .collect(Collectors.groupingBy(SeatBookingEntity::getBookingId));

        // Extract unique show IDs
        Set<Long> showIds = allSeatBookings.stream()
                .map(SeatBookingEntity::getShowId)
                .collect(Collectors.toSet());

        // Batch fetch all shows
        Map<Long, Show> showMap = showIds.isEmpty() ? Map.of()
                : showRepository.findAllById(showIds).stream()
                .collect(Collectors.toMap(Show::getId, show -> show));

        // Extract unique movie IDs
        Set<Long> movieIds = showMap.values().stream()
                .map(Show::getMovieId)
                .collect(Collectors.toSet());

        // Batch fetch all movies
        Map<Long, Movie> movieMap = movieIds.isEmpty() ? Map.of()
                : movieRepository.findAllById(movieIds).stream()
                .collect(Collectors.toMap(Movie::getId, movie -> movie));

        // Transform to DTOs
        return bookingPage.map(bookingEntity ->
                toBookingHistoryDTO(bookingEntity, seatBookingsByBookingId, showMap, movieMap)
        );
    }

    private BookingHistoryDTO toBookingHistoryDTO(
            BookingEntity bookingEntity,
            Map<Long, List<SeatBookingEntity>> seatBookingsByBookingId,
            Map<Long, Show> showMap,
            Map<Long, Movie> movieMap) {

        // 1. Get seat bookings from the pre-fetched map
        List<SeatBookingEntity> seatBookings = seatBookingsByBookingId
                .getOrDefault(bookingEntity.getBookingId(), List.of());

        // 2. Extract showId and seatNumbers
        Long showId = seatBookings.isEmpty() ? null : seatBookings.get(0).getShowId();
        List<String> seats = seatBookings.stream()
                .map(SeatBookingEntity::getSeatNumber)
                .toList();

        // 3. Get movie title from pre-fetched maps
        String movieTitle = null;
        if (showId != null) {
            Show show = showMap.get(showId);
            if (show != null) {
                Movie movie = movieMap.get(show.getMovieId());
                if (movie != null) {
                    movieTitle = movie.getTitle();
                }
            }
        }

        // 4. Total price
        Double totalPrice = bookingEntity.getTotalPrice();

        return new BookingHistoryDTO(
                bookingEntity.getBookingId(),
                bookingEntity.getBookingStatus().name(),
                showId,
                movieTitle,
                seats,
                totalPrice,
                bookingEntity.getCreatedAt()
        );
    }
}
