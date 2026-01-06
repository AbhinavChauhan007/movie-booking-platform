package com.abhinav.moviebooking.pricing.context;

import com.abhinav.moviebooking.pricing.strategy.PremiumUserPricingStrategy;
import com.abhinav.moviebooking.pricing.strategy.PricingStrategy;
import com.abhinav.moviebooking.pricing.strategy.RegularPricingStrategy;
import com.abhinav.moviebooking.pricing.strategy.WeekendPricingStrategy;
import org.springframework.stereotype.Component;

@Component
public class PricingContext {

    public PricingStrategy resolve(PricingRequest request) {

        if (request.isPremiumUser())
            return new PremiumUserPricingStrategy();

        if (request.isWeekend())
            return new WeekendPricingStrategy();

        return new RegularPricingStrategy();

    }
}
