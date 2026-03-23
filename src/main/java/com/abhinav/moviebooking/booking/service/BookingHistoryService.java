package com.abhinav.moviebooking.booking.service;

import com.abhinav.moviebooking.booking.dto.response.BookingHistoryDTO;
import com.abhinav.moviebooking.booking.persistence.entity.BookingEntity;
import com.abhinav.moviebooking.booking.persistence.entity.SeatBookingEntity;
import com.abhinav.moviebooking.booking.persistence.repository.BookingRepository;
import com.abhinav.moviebooking.booking.persistence.repository.SeatBookingRepository;
import com.abhinav.moviebooking.movie.entity.Movie;
import com.abhinav.moviebooking.movie.repository.MovieRepository;
import com.abhinav.moviebooking.show.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<BookingHistoryDTO> getUserBookingHistory(Long userId) {
        // 1. fetch all bookings for user
        List<BookingEntity> bookings = bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        // 2. Transform each booking to DTO
        return bookings.stream()
                .map(this::toBookingHistoryDTO)
                .collect(Collectors.toList());

    }

    private BookingHistoryDTO toBookingHistoryDTO(BookingEntity bookingEntity) {

        // 1. fetch seatBookings for this booking
        List<SeatBookingEntity> seatBookings = seatBookingRepository.findAllByBookingId(bookingEntity.getBookingId());

        // 2. Extract showId and seatNumbers
        Long showId = seatBookings.isEmpty() ? null : seatBookings.get(0).getShowId();
        List<String> seats = seatBookings.stream()
                .map(SeatBookingEntity::getSeatNumber)
                .toList();

        // 3. Fetch movie title via show
        String movieTitle = null;
        if (showId != null) {
            movieTitle = showRepository.findById(showId)
                    .flatMap(show -> movieRepository.findById(show.getMovieId()))
                    .map(Movie::getTitle)
                    .orElse(null);
        }

        // 4. total Price - currently set to null
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
