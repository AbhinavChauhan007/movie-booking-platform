package com.abhinav.moviebooking.pricing.context;

import com.abhinav.moviebooking.pricing.strategy.PremiumUserPricingStrategy;
import com.abhinav.moviebooking.pricing.strategy.RegularPricingStrategy;
import com.abhinav.moviebooking.pricing.strategy.WeekendPricingStrategy;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PricingContextTest {

    private final PricingContext pricingContext = new PricingContext();

    @Test
    void testPremiumUserPricingStrategy() {
        PricingRequest request = new PricingRequest(1000, false, true);
        assertTrue(
                pricingContext.resolve(request)
                        instanceof PremiumUserPricingStrategy
        );
    }

    @Test
    void testWeekendPricingStrategy() {
        PricingRequest request = new PricingRequest(1000, true, false);
        assertTrue(
                pricingContext.resolve(request)
                        instanceof WeekendPricingStrategy);
    }

    @Test
    void testRegularPricingStrategy() {
        PricingRequest request = new PricingRequest(1000, false, false);
        assertTrue(
                pricingContext.resolve(request)
                        instanceof RegularPricingStrategy);
    }

}
