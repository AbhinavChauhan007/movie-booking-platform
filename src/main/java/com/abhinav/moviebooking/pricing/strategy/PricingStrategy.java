package com.abhinav.moviebooking.pricing.strategy;

import com.abhinav.moviebooking.pricing.context.PricingRequest;

public interface PricingStrategy {

    double calculatePrice(PricingRequest request);
}
