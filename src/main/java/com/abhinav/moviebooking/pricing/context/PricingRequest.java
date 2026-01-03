package com.abhinav.moviebooking.pricing.context;

public class PricingRequest {

    private final double basePrice;
    private final boolean isWeekend;
    private final boolean isPremium;

    public PricingRequest(double basePrice, boolean isWeekend, boolean isPremium) {
        this.basePrice = basePrice;
        this.isWeekend = isWeekend;
        this.isPremium = isPremium;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public boolean isWeekend() {
        return isWeekend;
    }

    public boolean isPremiumUser() {
        return isPremium;
    }
}
