package com.abhinav.moviebooking.pricing.strategy;

import com.abhinav.moviebooking.pricing.context.PricingRequest;

public class WeekendPricingStrategy implements PricingStrategy {

    // + 20% on weekends
    @Override
    public double calculatePrice(PricingRequest request) {
        return request.getBasePrice() * 1.20;
    }
}
