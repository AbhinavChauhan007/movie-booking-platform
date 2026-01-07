package com.abhinav.moviebooking.booking.state;

import com.abhinav.moviebooking.booking.domain.BookingStatus;
import com.abhinav.moviebooking.booking.state.impl.*;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class BookingStateFactory {

    private final Map<BookingStatus, BookingState> stateMap;

    public BookingStateFactory(InitiatedState initiatedState, ConfirmedState confirmedState, CancelledState cancelledState, ExpiredState expiredState) {
        this.stateMap = new EnumMap<>(BookingStatus.class);
        stateMap.put(BookingStatus.INITIATED, initiatedState);
        stateMap.put(BookingStatus.CONFIRMED, confirmedState);
        stateMap.put(BookingStatus.CANCELLED, cancelledState);
        stateMap.put(BookingStatus.EXPIRED, expiredState);
    }

    public BookingState getBookingState(BookingStatus status) {
        return stateMap.get(status);
    }
}
