package com.abhinav.moviebooking.booking.seat.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SeatAllocationStrategyFactory {

    private final Map<SeatType, SeatAllocationStrategy> seatAllocationStrategyMap;

    public SeatAllocationStrategyFactory(List<SeatAllocationStrategy> strategies) {
        this.seatAllocationStrategyMap =
                strategies
                        .stream()
                        .collect(Collectors.toMap(
                                SeatAllocationStrategy::getSeatType, Function.identity()));
    }

    /**
     * Factory method to get seat allocation strategy based on seat type
     *
     * @param seatType type of seat allocation
     * @return corresponding SeatAllocationStrategy implementation
     */
    public SeatAllocationStrategy getStrategy(SeatType seatType) {
        SeatAllocationStrategy seatAllocationStrategy = seatAllocationStrategyMap.get(seatType);

        if (seatAllocationStrategy == null) {
            throw new IllegalArgumentException("No SeatAllocationStrategy found for type: " + seatType);
        }
        return seatAllocationStrategy;
    }

}
