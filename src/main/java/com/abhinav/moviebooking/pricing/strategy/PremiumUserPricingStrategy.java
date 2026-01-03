package com.abhinav.moviebooking.pricing.strategy;

import com.abhinav.moviebooking.pricing.context.PricingRequest;

public class PremiumUserPricingStrategy implements PricingStrategy {

    // 10% discounts for premium user
    @Override
    public double calculatePrice(PricingRequest request) {
        return request.getBasePrice() * 0.90;
    }
}
