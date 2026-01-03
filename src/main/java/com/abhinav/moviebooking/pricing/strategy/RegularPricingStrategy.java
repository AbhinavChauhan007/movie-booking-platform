package com.abhinav.moviebooking.pricing.strategy;

import com.abhinav.moviebooking.pricing.context.PricingRequest;

public class RegularPricingStrategy implements PricingStrategy{

    @Override
    public double calculatePrice(PricingRequest request) {
        return request.getBasePrice();
    }
}
