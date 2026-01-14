package com.abhinav.moviebooking.booking.bookingTest;

import com.abhinav.moviebooking.booking.seat.SeatAllocationStrategy;
import com.abhinav.moviebooking.booking.seat.SeatAllocationStrategyFactory;
import com.abhinav.moviebooking.booking.seat.SeatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SeatAllocationStrategyFactoryTest {

    @Test
    @DisplayName("Factory should return BEST_AVAILABLE strategy")
    void shouldReturnBestAvailableStrategy() {
        // given
        SeatAllocationStrategy bestAvailableStrategy = mock(SeatAllocationStrategy.class);

        when(bestAvailableStrategy.getSeatType()).thenReturn(SeatType.BEST_AVAILABLE);

        SeatAllocationStrategyFactory factory = new SeatAllocationStrategyFactory(List.of(bestAvailableStrategy));

        // when
        SeatAllocationStrategy result = factory.getStrategy(SeatType.BEST_AVAILABLE);

        // then
        assertNotNull(result);
        assertEquals(bestAvailableStrategy, result);
    }

    @Test
    @DisplayName("Factory should return FIRST_AVAILABLE strategy")
    void shouldReturnFirstAvailableStrategy() {
        // given
        SeatAllocationStrategy firstAvailableStrategy = mock(SeatAllocationStrategy.class);

        when(firstAvailableStrategy.getSeatType()).thenReturn(SeatType.FIRST_AVAILABLE);

        SeatAllocationStrategyFactory factory = new SeatAllocationStrategyFactory(List.of(firstAvailableStrategy));

        // when
        SeatAllocationStrategy result = factory.getStrategy(SeatType.FIRST_AVAILABLE);

        // then
        assertNotNull(result);
        assertEquals(firstAvailableStrategy, result);
    }

    @Test
    @DisplayName("Factory should throw exception for unsupported seat type")
    void shouldThrowExceptionForUnsupportedSeatType() {
        // given
        SeatAllocationStrategy bestAvailableStrategy = mock(SeatAllocationStrategy.class);
        when(bestAvailableStrategy.getSeatType()).thenReturn(SeatType.BEST_AVAILABLE);

        SeatAllocationStrategyFactory factory = new SeatAllocationStrategyFactory(List.of(bestAvailableStrategy));

        // when + then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy(SeatType.FIRST_AVAILABLE));

        assertEquals("No SeatAllocationStrategy found for type: FIRST_AVAILABLE",
                ex.getMessage());
    }

    @Test
    @DisplayName("Factory should support multiple strategies")
    void shouldSupportMultipleStrategies() {
        SeatAllocationStrategy firstAvailableStrategy = mock(SeatAllocationStrategy.class);
        when(firstAvailableStrategy.getSeatType()).thenReturn(SeatType.FIRST_AVAILABLE);

        SeatAllocationStrategy bestAvailableStrategy = mock(SeatAllocationStrategy.class);
        when(bestAvailableStrategy.getSeatType()).thenReturn(SeatType.BEST_AVAILABLE);

        SeatAllocationStrategyFactory factory =
                new SeatAllocationStrategyFactory(
                        List.of(firstAvailableStrategy, bestAvailableStrategy)
                );

        // when
        SeatAllocationStrategy result1 = factory.getStrategy(SeatType.BEST_AVAILABLE);
        SeatAllocationStrategy result2 = factory.getStrategy(SeatType.FIRST_AVAILABLE);

        // then
        assertEquals(result1, bestAvailableStrategy);
        assertEquals(result2, firstAvailableStrategy);
    }


}
